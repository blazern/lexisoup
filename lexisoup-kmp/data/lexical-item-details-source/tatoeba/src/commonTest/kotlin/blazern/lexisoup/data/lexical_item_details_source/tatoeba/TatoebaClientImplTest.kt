package blazern.lexisoup.data.lexical_item_details_source.tatoeba

import arrow.core.Either
import arrow.core.getOrElse
import blazern.lexisoup.core.ktor.KtorClientHolder
import blazern.lexisoup.domain.backend_address.BackendAddressProvider
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_MAX
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals

class TatoebaClientImplTest {
    private lateinit var response: Either<IOException, String>

    private val mockEngine = MockEngine {
        response.fold(
            { throw it },
            {
                respond(
                    content = ByteReadChannel(it),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        )

    }

    private val kotlinClientHolder = KtorClientHolder(mockEngine)
    private val tatoeba = TatoebaClientImpl(kotlinClientHolder, FakeBackendAddressProvider())

    @Test
    fun `search returns only langTo translations`() = runTest {
        setResponse("""
            {
              "results": [
                {
                  "text": "Hello!",
                  "lang": "eng",
                  "translations": [
                    [
                      { "text": "Hallo!", "lang": "deu" },
                      { "text": "Grüß Gott!", "lang": "deu" },
                      { "text": "Salut!", "lang": "rus" }
                    ]
                  ]
                },
                {
                  "text": "Hello there",
                  "lang": "eng",
                  "translations": [
                    [
                      { "text": "Guten Tag", "lang": "deu" }
                    ]
                  ]
                },
                {
                  "text": "Good morning.",
                  "lang": "eng",
                  "translations": [
                    [
                      { "text": "Bonjour.", "lang": "rus" }
                    ]
                  ]
                }
              ]
            }
        """.trimIndent())

        val result = tatoeba
            .search("hello", Lang.EN, Lang.DE, page = 0)
            .getOrElse { throw it.e!! }

        val expected = listOf(
            TranslationsSet(
                original = Sentence("Hello!", Lang.EN, DataSource.TATOEBA),
                translations = listOf(
                    Sentence("Hallo!", Lang.DE, DataSource.TATOEBA),
                    Sentence("Grüß Gott!", Lang.DE, DataSource.TATOEBA),
                ),
                translationsQualities = listOf(QUALITY_MAX, QUALITY_MAX),
            ),
            TranslationsSet(
                original = Sentence("Hello there", Lang.EN, DataSource.TATOEBA),
                translations = listOf(
                    Sentence("Guten Tag", Lang.DE, DataSource.TATOEBA),
                ),
                translationsQualities = listOf(QUALITY_MAX),
            ),
        )

        assertEquals(expected, result)
    }

    @Test
    fun `empty results`() = runTest {
        setResponse("""{ "results": [] }""")

        val result = tatoeba
            .search("hello", Lang.EN, Lang.DE, page = 0)
            .getOrElse { throw it.e!! }
        val expected = emptyList<TranslationsSet>()
        assertEquals(expected, result)
    }

    @Test
    fun exception() = runTest {
        val e = IOException("no internet")
        setResponse(e)

        val result = tatoeba.search("hello", Lang.EN, Lang.DE, page = 0)
        val expected = Either.Left(e)
        assertEquals(expected.value, result.leftOrNull()!!.e!!.cause)
    }

    private fun setResponse(json: String) {
        response = Either.Right(json)
    }

    private fun setResponse(exception: IOException) {
        response = Either.Left(exception)
    }
}

private class FakeBackendAddressProvider(
    override val baseUrl: Flow<String> = flowOf("my.webserver.com"),
    override val isLocalhost: Flow<Boolean> = flowOf(false),
) : BackendAddressProvider {
    override suspend fun setIsLocalhost(isLocalhost: Boolean) = throw NotImplementedError()
}
