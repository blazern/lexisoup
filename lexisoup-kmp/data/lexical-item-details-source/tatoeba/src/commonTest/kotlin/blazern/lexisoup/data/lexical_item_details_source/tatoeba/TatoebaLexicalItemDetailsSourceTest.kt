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
            original = Sentence("Hello", Lang.EN, DataSource.Tatoeba),
            translations = listOf(Sentence("Hallo", Lang.DE, DataSource.Tatoeba)),
            translationsQualities = listOf(QUALITY_MAX),
        ),
        TranslationsSet(
            original = Sentence("Good morning", Lang.EN, DataSource.Tatoeba),
            translations = listOf(Sentence("Guten Morgen", Lang.DE, DataSource.Tatoeba)),
            translationsQualities = listOf(QUALITY_MAX),
        ),
    )

    @Test
    fun `source and types`() = runTest {
        assertEquals(DataSource.Tatoeba, source.source)
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
                source = DataSource.Tatoeba,
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
    fun `paginates and retries same page after error`() = runTest {
        val page1 = listOf(
            TranslationsSet(
                original = Sentence("Hello", Lang.EN, DataSource.Tatoeba),
                translations = listOf(Sentence("Hallo", Lang.DE, DataSource.Tatoeba)),
                translationsQualities = listOf(QUALITY_MAX),
            )
        )
        val page2 = listOf(
            TranslationsSet(
                original = Sentence("Hi", Lang.EN, DataSource.Tatoeba),
                translations = listOf(Sentence("Hi!", Lang.DE, DataSource.Tatoeba)),
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
            LexicalItemDetail.Example(page1[0], DataSource.Tatoeba),
            first.details[0]
        )

        // First attempt of Page 2 emits an error
        assertTrue(iter.next() is Item.Failure)

        // Retry of Page 2 succeeds and emits its examples
        val third = iter.next() as Item.Page
        assertEquals(
            LexicalItemDetail.Example(page2[0], DataSource.Tatoeba),
            third.details[0]
        )

        iter.close()
    }

    @Test
    fun `uses forms to refine Tatoeba query`() = runTest {
        val translations = TranslationsSet(
            original = Sentence("sie lachen", Lang.DE, DataSource.Tatoeba),
            translations = listOf(Sentence("they laugh", Lang.EN, DataSource.Tatoeba)),
            translationsQualities = listOf(QUALITY_MAX),
        )
        val forms = listOf(
            WordForm(
                text = "lachen",
                lang = Lang.DE
            ),
            WordForm(
                text = "lache",
                lang = Lang.DE
            ),
            WordForm(
                text = "lachst",
                lang = Lang.DE,
            ),
        )
        formsProvider.nextResult = Right(forms)

        // First wave of requests is for the most important form always
        tatoeba.enqueueResult(
            query = "=lachen",
            langFrom = Lang.DE,
            langTo = Lang.EN,
            page = 1,
            result = Right(listOf(translations)),
        )
        tatoeba.enqueueResult(
            query = "=lachen",
            langFrom = Lang.DE,
            langTo = Lang.EN,
            page = 2,
            result = Right(emptyList()),
        )
        // Then come requests for other forms
        tatoeba.enqueueResult(
            query = "(=lache|=lachst)",
            langFrom = Lang.DE,
            langTo = Lang.EN,
            page = 1,
            result = Right(listOf(translations)),
        )
        tatoeba.enqueueResult(
            query = "(=lache|=lachst)",
            langFrom = Lang.DE,
            langTo = Lang.EN,
            page = 2,
            result = Right(emptyList()),
        )
        source.request("lach", Lang.DE, Lang.EN).toList()

        val calls = tatoeba.calls
        assertEquals(4, calls.size)
        assertEquals("=lachen", calls[0].query)
        assertEquals("=lachen", calls[1].query)
        assertEquals(1, calls[0].page)
        assertEquals(2, calls[1].page)
        assertEquals("(=lache|=lachst)", calls[2].query)
        assertEquals("(=lache|=lachst)", calls[3].query)
        assertEquals(1, calls[2].page)
        assertEquals(2, calls[3].page)
    }

    @Test
    fun `forms in the Tatoeba query are deduplicated`() = runTest {
        val translations = TranslationsSet(
            original = Sentence("sie lachen", Lang.DE, DataSource.Tatoeba),
            translations = listOf(Sentence("they laugh", Lang.EN, DataSource.Tatoeba)),
            translationsQualities = listOf(QUALITY_MAX),
        )
        val forms = listOf(
            WordForm(
                text = "lachen",
                lang = Lang.DE
            ),
            WordForm(
                text = "lacht",
                lang = Lang.DE,
                tags = listOf(WordForm.Tag.Defined.Masculine("")),
            ),
            WordForm(
                text = "lacht",
                lang = Lang.DE,
                tags = listOf(WordForm.Tag.Defined.Feminine("")),
            ),
        )
        formsProvider.nextResult = Right(forms)

        // Main form request
        tatoeba.enqueueResult(
            query = "=lachen",
            langFrom = Lang.DE,
            langTo = Lang.EN,
            page = 1,
            result = Right(emptyList()),
        )
        tatoeba.enqueueResult(
            // Secondary forms are deduplicated
            query = "(=lacht)",
            langFrom = Lang.DE,
            langTo = Lang.EN,
            page = 1,
            result = Right(emptyList()),
        )
        source.request("lachen", Lang.DE, Lang.EN).toList()

        val calls = tatoeba.calls
        assertEquals(2, calls.size)
        assertEquals("=lachen", calls[0].query)
        assertEquals("(=lacht)", calls[1].query)
    }
}
