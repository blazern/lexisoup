package blazern.lexisoup.data.lexical_item_details_source.kaikki

import arrow.core.Either.Left
import arrow.core.Either.Right
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.data.kaikki.model.Entry
import blazern.lexisoup.data.kaikki.model.Example
import blazern.lexisoup.data.kaikki.model.Form
import blazern.lexisoup.data.kaikki.model.FormOf
import blazern.lexisoup.data.kaikki.model.Related
import blazern.lexisoup.data.kaikki.model.Sense
import blazern.lexisoup.data.kaikki.model.Translation
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource
import blazern.lexisoup.data.lexical_item_details_source.utils.cache.LexicalItemDetailsSourceCacher
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.LexicalItemDetail.Forms
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_MAX
import blazern.lexisoup.domain.model.WordForm
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Plural
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Singular
import blazern.lexisoup.utils.FlowIterator
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KaikkiLexicalItemDetailsSourceImplTest {
    private val kaikkiClient = FakeKaikkiClient()
    private val source: LexicalItemDetailsSource = KaikkiLexicalItemDetailsSourceImpl(
        kaikkiClient,
        LexicalItemDetailsSourceCacher.NOOP,
    )

    private val entry = Entry(
        word = "Haus",
        pos = "noun",
        posTitle = "Substantiv",
        langCode = "de",
        lang = "German",
        senses = listOf(
            Sense(
                glosses = listOf("house; building"),
                examples = listOf(
                    Example("Das ist ein Haus.")
                )
            )
        ),
        forms = listOf(
            Form("das Haus", tags = listOf("singular")),
            Form("Häuser", tags = listOf("plural")),
        ),
        translations = listOf(
            Translation(word = "house", langCode = Lang.EN.iso2),
            Translation(word = "дом", langCode = Lang.RU.iso2),
        ),
        synonyms = listOf(
            Related("Gebäude"),
            Related("Hütte"),
        ),
        coordinateTerms = listOf(
            Related("Hochhaus"),
            Related("Einfamilienhaus"),
        ),
    )

    @Test
    fun `source id and supported types`() = runTest {
        assertEquals(DataSource.KAIKKI, source.source)
        assertEquals(
            setOf(
                LexicalItemDetail.Type.FORMS,
                LexicalItemDetail.Type.EXPLANATION,
                LexicalItemDetail.Type.EXAMPLE,
            ),
            source.types
        )
    }

    @Test
    fun `parses all expected fields`() = runTest {
        kaikkiClient.enqueue("Haus", Lang.DE, Right(listOf(entry)))

        val results = source.request("Haus", Lang.DE, Lang.EN)
            .toList()
            .map { (it as Item.Page).details }
            .flatten()

        val expected = listOf(
            Forms(
                Forms.Value.Detailed(listOf(
                    WordForm("das Haus", listOf(Singular("singular")), Lang.DE),
                    WordForm("Häuser", listOf(Plural("plural")), Lang.DE),
                )),
                DataSource.KAIKKI,
            ),
            LexicalItemDetail.Explanation("house; building", DataSource.KAIKKI),
            LexicalItemDetail.Example(
                TranslationsSet(
                    original = Sentence("Das ist ein Haus.", Lang.EN, DataSource.KAIKKI),
                    translations = emptyList(),
                    translationsQualities = emptyList(),
                ),
                DataSource.KAIKKI,
            ),
            LexicalItemDetail.WordTranslations(
                TranslationsSet(
                    original = Sentence("Haus", Lang.DE, DataSource.KAIKKI),
                    translations = listOf(Sentence("house", Lang.EN, DataSource.KAIKKI)),
                    translationsQualities = listOf(QUALITY_MAX),
                ),
                DataSource.KAIKKI,
            ),
            LexicalItemDetail.Synonyms(
                TranslationsSet(
                    original = Sentence("Haus", Lang.DE, DataSource.KAIKKI),
                    translations = listOf(
                        Sentence("Gebäude", Lang.DE, DataSource.KAIKKI),
                        Sentence("Hütte", Lang.DE, DataSource.KAIKKI),
                        Sentence("Hochhaus", Lang.DE, DataSource.KAIKKI),
                        Sentence("Einfamilienhaus", Lang.DE, DataSource.KAIKKI),
                    ),
                    translationsQualities = listOf(
                        QUALITY_MAX,
                        QUALITY_MAX,
                        QUALITY_MAX,
                        QUALITY_MAX,
                    ),
                ),
                DataSource.KAIKKI,
            ),
        )

        assertEquals(expected, results)
    }

    @Test
    fun `recovers after initial error`() = runTest {
        val io = IOException("no internet")
        // First call to search("Haus", Lang.DE) -> Left(...)
        // Second call -> Right(listOf(entry))
        kaikkiClient.enqueue(
            query = "Haus",
            langFrom = Lang.DE,
            Left(Err.from(io)),
            Right(listOf(entry)),
        )

        val flow = source.request("Haus", Lang.DE, Lang.EN)
        val iter = FlowIterator(flow)

        assertTrue(iter.next() is Item.Failure)
        assertTrue(iter.next() is Item.Page)
        iter.close()
    }

    @Test
    fun `purely word-form entry skips own details and recurses to parent`() = runTest {
        val purelyWordFormEntry = Entry(
            word = "Häuser",
            pos = "noun",
            posTitle = "Substantiv",
            langCode = "de",
            lang = "German",
            senses = listOf(
                Sense(
                    glosses = emptyList(),
                    examples = emptyList(),
                    formOf = listOf(FormOf(word = "Haus"))
                )
            ),
            forms = listOf(
                // Should NOT be emitted because the entry is purely a word form.
                Form(form = "Häuser", tags = listOf("plural"))
            )
        )

        // Entry "Haus" with real details.
        val lemmaEntry = Entry(
            word = "Haus",
            pos = "noun",
            posTitle = "Substantiv",
            langCode = "de",
            lang = "German",
            senses = listOf(
                Sense(
                    glosses = listOf("house; building"),
                    examples = listOf(Example("Das ist ein Haus."))
                )
            ),
            forms = listOf(Form(form = "Häuser", tags = listOf("plural")))
        )

        kaikkiClient.enqueue(
            query = "Häuser",
            langFrom = Lang.DE,
            Right(listOf(purelyWordFormEntry)),
        )
        kaikkiClient.enqueue(
            query = "Haus",
            langFrom = Lang.DE,
            Right(listOf(lemmaEntry)),
        )

        val results = source.request("Häuser", Lang.DE, Lang.EN)
            .toList()
            .map { (it as Item.Page).details }
            .flatten()

        // Expect ONLY the parent
        val expected = listOf(
            Forms(
                Forms.Value.Detailed(listOf(
                    WordForm("Häuser", listOf(Plural("plural")), Lang.DE),
                )),
                DataSource.KAIKKI,
            ),
            LexicalItemDetail.Explanation("house; building", DataSource.KAIKKI),
            LexicalItemDetail.Example(
                TranslationsSet(
                    original = Sentence("Das ist ein Haus.", Lang.EN, DataSource.KAIKKI),
                    translations = emptyList(),
                    translationsQualities = emptyList(),
                ),
                DataSource.KAIKKI
            )
        )

        assertEquals(expected, results)
    }
}
