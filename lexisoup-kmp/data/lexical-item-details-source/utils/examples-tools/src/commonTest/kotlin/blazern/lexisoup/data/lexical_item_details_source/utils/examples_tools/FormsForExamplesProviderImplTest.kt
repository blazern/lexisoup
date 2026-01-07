package blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools

import arrow.core.getOrElse
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.data.lexical_item_details_source.kaikki.KaikkiLexicalItemDetailsSource
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.LexicalItemDetail.Explanation
import blazern.lexisoup.domain.model.LexicalItemDetail.Forms
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.WordForm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FormsForExamplesProviderImplTest {
    private val types = setOf(LexicalItemDetail.Type.FORMS)
    private val kaikki = FakeKaikkiLexicalItemDetailsSource()
    private val formsProvider = FormsForExamplesProviderImpl(kaikki)

    @Test
    fun `uses and modifies Kaikki forms`() = runTest {
        val kaikkiForms = listOf(
            WordForm(
                text = "ich lache",
                tags = emptyList(),
                lang = Lang.DE
            ),
            WordForm(
                text = "du lachst",
                tags = emptyList(),
                lang = Lang.DE,
            ),
            WordForm("haben", listOf(WordForm.Tag.Defined.Auxiliary("aux")), Lang.DE),
            WordForm("der Lachen", emptyList(), Lang.DE),
        )
        val formsDetail = Forms(Forms.Value.Detailed(kaikkiForms), DataSource.KAIKKI)
        kaikki.responseFlow = flowOf(
            Item.Page(details = listOf(formsDetail), types)
        )

        val expectedForms = listOf(
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
            WordForm("Lachen", emptyList(), Lang.DE),
        )
        assertEquals(
            expectedForms,
            formsProvider.requestFor("lachen", Lang.DE, Lang.EN).getOrElse { throw it.e!! },
        )
    }

    @Test
    fun `iterats over flow until receives forms`() = runTest {
        val kaikkiForms = listOf(WordForm("lachen", emptyList(), Lang.DE))
        val formsDetail = Forms(Forms.Value.Detailed(kaikkiForms), DataSource.KAIKKI)
        kaikki.responseFlow = flowOf(
            Item.Page(details = listOf(Explanation("Explanation", DataSource.KAIKKI)), types),
            Item.Page(details = listOf(LexicalItemDetail.Example(TranslationsSet(
                Sentence("", Lang.DE, DataSource.KAIKKI), emptyList(), emptyList(),
            ), DataSource.KAIKKI)), types),
            Item.Page(details = listOf(formsDetail), types),
        )

        assertEquals(
            kaikkiForms,
            formsProvider.requestFor("lachen", Lang.DE, Lang.EN).getOrElse { throw it.e!! },
        )
    }

    @Test
    fun `first error stops requests`() = runTest {
        val kaikkiForms = listOf(WordForm("lachen", emptyList(), Lang.DE))
        val formsDetail = Forms(Forms.Value.Detailed(kaikkiForms), DataSource.KAIKKI)
        kaikki.responseFlow = flowOf(
            Item.Page(details = listOf(Explanation("Explanation", DataSource.KAIKKI)), types),
            Item.Failure(Err.from(Exception())),
            Item.Page(details = listOf(formsDetail), types),
        )

        val result = formsProvider.requestFor("lachen", Lang.DE, Lang.EN)
        assertTrue(result.isLeft())
    }
}

private class FakeKaikkiLexicalItemDetailsSource : KaikkiLexicalItemDetailsSource {
    var responseFlow: Flow<Item> = emptyFlow()

    override fun request(
        query: String,
        langFrom: Lang,
        langTo: Lang
    ) = responseFlow
}
