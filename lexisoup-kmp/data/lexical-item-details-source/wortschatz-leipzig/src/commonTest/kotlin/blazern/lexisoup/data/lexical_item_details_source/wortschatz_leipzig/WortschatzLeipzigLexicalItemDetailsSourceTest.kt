package blazern.lexisoup.data.lexical_item_details_source.wortschatz_leipzig


import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import blazern.lexisoup.core.ktor.KtorClientHolder
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.domain.backend_address.BackendAddressProvider
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.WordForm
import blazern.lexisoup.utils.FlowIterator
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WortschatzLeipzigLexicalItemDetailsSourceTest {
    private val capturedUrls = mutableListOf<String>()
    private val responses = mutableListOf<Either<Exception, Pair<HttpStatusCode, String>>>()

    private val mockEngine = MockEngine { request ->
        capturedUrls.add(request.url.toString())
        val next = responses.removeFirstOrNull()
            ?: Right(HttpStatusCode.OK to """{"count":0,"sentences":[]}""")
        next.fold(
            { throw it },
            { (code, body) ->
                respond(
                    content = ByteReadChannel(body),
                    status = code,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        )
    }

    private val clientHolder = KtorClientHolder(mockEngine)
    private val formsProvider = FakeFormsForExamplesProvider()
    private val source: LexicalItemDetailsSource = WortschatzLeipzigLexicalItemDetailsSource(
        FakeBackendAddressProvider(),
        clientHolder,
        formsProvider,
    )

    private fun enqueue(body: String, code: HttpStatusCode = HttpStatusCode.OK) {
        responses.add(Right(code to body))
    }

    private fun enqueueError(e: Exception) {
        responses.add(Left(e))
    }

    private fun exampleJson(vararg pairs: Pair<String, String>): String {
        val items = pairs.joinToString(",") { (id, sent) ->
            """{"id":"$id","sentence":"$sent"}"""
        }
        return """{"count":${pairs.size},"sentences":[$items]}"""
    }

    private fun example(text: String, lang: Lang) = LexicalItemDetail.Example(
        translationsSet = TranslationsSet(
            original = Sentence(text, lang, DataSource.WORTSCHATZ_LEIPZIG),
            translations = emptyList(),
            translationsQualities = emptyList(),
        ),
        source = DataSource.WORTSCHATZ_LEIPZIG
    )

    @BeforeTest
    fun setUp() {
        responses.clear()
    }

    @Test
    fun `source id and supported types`() = runTest {
        assertEquals(DataSource.WORTSCHATZ_LEIPZIG, source.source)
        assertEquals(setOf(LexicalItemDetail.Type.EXAMPLE), source.types)
    }

    @Test
    fun `emits examples from a single page and deduplicates within page`() = runTest {
        enqueue(
            exampleJson(
                "42" to "Cat sits",
                "42" to "Cat sits 2",
                "43" to "Cat sleeps"
            )
        )

        val results = source.request("cat", Lang.EN, Lang.RU)
            .toList()
            .map { (it as Item.Page).details }
            .flatten()

        val expected = listOf(
            example("Cat sits", Lang.EN),
            example("Cat sleeps", Lang.EN)
        )
        assertEquals(expected, results)
    }

    @Test
    fun `paginates until short page`() = runTest {
        // LIMIT = 10; first page has 10 items, second has 3 -> stop
        enqueue(exampleJson(*(0 until 10).map { "p1-$it" to "Satz $it" }.toTypedArray()))
        enqueue(exampleJson("p2-0" to "Satz 10", "p2-1" to "Satz 11", "p2-2" to "Satz 12"))

        val results = source.request("Haus", Lang.DE, Lang.EN)
            .toList()
            .map { (it as Item.Page).details }
            .flatten()

        val expected = buildList {
            addAll((0 until 10).map { example("Satz $it", Lang.DE) })
            addAll(listOf(
                example("Satz 10", Lang.DE),
                example("Satz 11", Lang.DE),
                example("Satz 12", Lang.DE),
            ))
        }
        assertEquals(expected, results)
    }

    @Test
    fun `deduplicates across pages`() = runTest {
        // First page has id=1..3; second page repeats id=3 and adds id=4
        enqueue(exampleJson("1" to "uno", "2" to "dos", "3" to "tres"))
        enqueue(exampleJson("3" to "tres (dup)", "4" to "cuatro"))

        val results = source.request("algo", Lang.EN, Lang.DE)
            .toList()
            .map { (it as Item.Page).details }
            .flatten()

        val expected = listOf(
            example("uno", Lang.EN),
            example("dos", Lang.EN),
            example("tres", Lang.EN),
            example("cuatro", Lang.EN),
        )
        assertEquals(expected, results)
    }

    @Test
    fun `recovers after initial error`() = runTest {
        enqueueError(IOException("no internet"))
        enqueue(exampleJson("100" to "First successful example"))

        val flow = source.request("кот", Lang.EN, Lang.RU)
        val iter = FlowIterator(flow)

        assertIs<Item.Failure>(iter.next())
        val next = iter.next()

        assertIs<Item.Page>(next)
        val right = next.details[0]
        assertEquals(example("First successful example", Lang.EN), right)

        iter.close()
    }

    @Test
    fun `uses forms to send multiple requests`() = runTest {
        val forms = listOf(
            WordForm(
                text = "lache",
                tags = emptyList(),
                lang = Lang.DE
            ),
            WordForm(
                text = "lachst",
                tags = emptyList(),
                lang = Lang.DE,
            ),
        )
        formsProvider.nextResult = Right(forms)

        enqueue(exampleJson("1" to "Ich lache."))
        enqueue(exampleJson("2" to "Du lachst."))

        source.request("lachen", Lang.DE, Lang.EN)
            .toList()
            .map { (it as Item.Page).details }
            .flatten()

        assertTrue(capturedUrls.any { it.contains("/sentences/lache") })
        assertTrue(capturedUrls.any { it.contains("/sentences/lachst") })
        assertTrue(capturedUrls.none { it.contains("/sentences/lachen") })
    }

    @Test
    fun `emits page only when there are new examples`() = runTest {
        // First page has 2 examples -> emits Page
        enqueue(exampleJson("1" to "eins", "2" to "zwei"))
        // Second page repeats both examples -> should NOT emit Page
        enqueue(exampleJson("1" to "eins", "2" to "zwei"))
        // Third page has 1 new example -> emits Page
        enqueue(exampleJson("3" to "drei"))

        val results = source.request("zahlen", Lang.DE, Lang.EN)
            .toList()
            .filterIsInstance<Item.Page>()
            .map { it.details }
            .flatten()

        val expected = listOf(
            example("eins", Lang.DE),
            example("zwei", Lang.DE),
            example("drei", Lang.DE),
        )
        assertEquals(expected, results)
    }
}

private class FakeBackendAddressProvider(
    override val baseUrl: Flow<String> = flowOf("my.webserver.com"),
    override val isLocalhost: Flow<Boolean> = flowOf(false),
) : BackendAddressProvider {
    override suspend fun setIsLocalhost(isLocalhost: Boolean) = throw NotImplementedError()
}
