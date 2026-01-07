package blazern.lexisoup.data.lexical_item_details_source.tatoeba


import arrow.core.Either.Left
import arrow.core.Either.Right
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_MAX
import blazern.lexisoup.domain.model.WordForm
import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.FormsForExamplesProvider
import blazern.lexisoup.utils.FlowIterator
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TatoebaLexicalItemDetailsSourceTest {
    private val tatoeba = FakeTatoebaClient()
    private val formsProvider = FakeFormsForExamplesProvider().apply {
        nextResult = Left(Err.from(Exception()))
    }
    private val source = TatoebaLexicalItemDetailsSource(
        tatoeba,
        formsProvider,
    )

    private val translationsSets = listOf(
        TranslationsSet(
            original = Sentence("Hello", Lang.EN, DataSource.TATOEBA),
            translations = listOf(Sentence("Hallo", Lang.DE, DataSource.TATOEBA)),
            translationsQualities = listOf(QUALITY_MAX),
        ),
        TranslationsSet(
            original = Sentence("Good morning", Lang.EN, DataSource.TATOEBA),
            translations = listOf(Sentence("Guten Morgen", Lang.DE, DataSource.TATOEBA)),
            translationsQualities = listOf(QUALITY_MAX),
        ),
    )

    @Test
    fun `source and types`() = runTest {
        assertEquals(DataSource.TATOEBA, source.source)
        assertEquals(setOf(LexicalItemDetail.Type.EXAMPLE), source.types)
    }

    @Test
    fun `good scenario`() = runTest {
        tatoeba.enqueueResult(
            query = "hello",
            langFrom = Lang.EN,
            langTo = Lang.DE,
            page = 1,
            result = Right(translationsSets),
        )
        tatoeba.enqueueResult(
            query = "hello",
            langFrom = Lang.EN,
            langTo = Lang.DE,
            page = 2,
            result = Right(emptyList()),
        )

        val results = source.request("hello", Lang.EN, Lang.DE)
            .toList()
            .map { (it as Item.Page).details }
            .flatten()

        val expected = translationsSets.map {
            LexicalItemDetail.Example(
                it,
                source = DataSource.TATOEBA,
            )
        }
        assertEquals(expected, results)
    }

    @Test
    fun `bad and then good scenario`() = runTest {
        // Bad
        tatoeba.enqueueResult(
            query = "hello",
            langFrom = Lang.EN,
            langTo = Lang.DE,
            page = 1,
            result = Left(Err.from(Exception())),
        )
        val flow = source.request("hello", Lang.EN, Lang.DE)
        val iter = FlowIterator(flow)
        assertTrue { iter.next() is Item.Failure }

        // Good
        tatoeba.enqueueResult(
            query = "hello",
            langFrom = Lang.EN,
            langTo = Lang.DE,
            page = 1,
            result = Right(translationsSets),
        )
        assertTrue { iter.next() is Item.Page }
        iter.close()
    }

    @Test
    fun `uses forms to refine Tatoeba query`() = runTest {
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

        tatoeba.enqueueResult(
            query = "(=lache|=lachst)",
            langFrom = Lang.DE,
            langTo = Lang.EN,
            page = 1,
            result = Right(emptyList()),
        )
        source.request("lachen", Lang.DE, Lang.EN).toList()

        val calls = tatoeba.calls
        assertEquals(1, calls.size)
        assertEquals("(=lache|=lachst)", calls[0].query)
    }

    @Test
    fun `paginates and retries same page after error`() = runTest {
        val page1 = listOf(
            TranslationsSet(
                original = Sentence("Hello", Lang.EN, DataSource.TATOEBA),
                translations = listOf(Sentence("Hallo", Lang.DE, DataSource.TATOEBA)),
                translationsQualities = listOf(QUALITY_MAX),
            )
        )
        val page2 = listOf(
            TranslationsSet(
                original = Sentence("Hi", Lang.EN, DataSource.TATOEBA),
                translations = listOf(Sentence("Hi!", Lang.DE, DataSource.TATOEBA)),
                translationsQualities = listOf(QUALITY_MAX),
            )
        )

        // Page 1 OK
        tatoeba.enqueueResult(
            query = "hello",
            langFrom = Lang.EN,
            langTo = Lang.DE,
            page = 1,
            result = Right(page1),
        )
        // Page 2 fails once then succeeds
        tatoeba.enqueueResult(
            query = "hello",
            langFrom = Lang.EN,
            langTo = Lang.DE,
            page = 2,
            result = Left(Err.from(Exception())),
        )
        tatoeba.enqueueResult(
            query = "hello",
            langFrom = Lang.EN,
            langTo = Lang.DE,
            page = 2,
            result = Right(page2),
        )
        // Page 3 empty => stop
        tatoeba.enqueueResult(
            query = "hello",
            langFrom = Lang.EN,
            langTo = Lang.DE,
            page = 3,
            result = Right(emptyList()),
        )

        val flow = source.request("hello", Lang.EN, Lang.DE)
        val iter = FlowIterator(flow)

        // Page 1 emits its examples
        val first = iter.next() as Item.Page
        assertEquals(
            LexicalItemDetail.Example(page1[0], DataSource.TATOEBA),
            first.details[0]
        )

        // First attempt of Page 2 emits an error
        assertTrue(iter.next() is Item.Failure)

        // Retry of Page 2 succeeds and emits its examples
        val third = iter.next() as Item.Page
        assertEquals(
            LexicalItemDetail.Example(page2[0], DataSource.TATOEBA),
            third.details[0]
        )

        iter.close()
    }
}
