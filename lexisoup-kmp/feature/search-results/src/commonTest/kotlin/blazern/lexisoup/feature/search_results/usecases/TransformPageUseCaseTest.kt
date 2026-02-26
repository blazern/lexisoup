package blazern.lexisoup.feature.search_results.usecases

import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.LexicalItemDetail.Forms
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.WordForm
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TransformPageUseCaseTest {

    private val useCase = TransformPageUseCase()

    @Test
    fun `filters out Forms Detailed when forms list is empty`() {
        val emptyDetailedForms: LexicalItemDetail = Forms(
            value = Forms.Value.Detailed(
                forms = emptyList(),
            ),
            lang = Lang.DE,
            source = DataSource.Kaikki,
        )

        val page = Item.Page(
            details = listOf(emptyDetailedForms),
            nextPageTypes = nextPageTypes(),
        )

        val result = useCase(page)
        assertEquals(emptyList(), result)
    }

    @Test
    fun `keeps Forms Detailed when forms list is not empty`() {
        val nonEmptyDetailedForms: LexicalItemDetail = Forms(
            value = Forms.Value.Detailed(
                forms = listOf(WordForm("go", Lang.EN))
            ),
            lang = Lang.EN,
            source = DataSource.Kaikki,
        )

        val page = Item.Page(
            details = listOf(nonEmptyDetailedForms),
            nextPageTypes = nextPageTypes(),
        )

        val result = useCase(page)
        assertEquals(listOf(page), result)
    }

    @Test
    fun `splits examples so each example becomes its own page and rest stays together`() {
        val example1 = exampleDetail("ex1")
        val example2 = exampleDetail("ex2")

        val rest1: LexicalItemDetail = nonExampleDetail("rest1")
        val rest2: LexicalItemDetail = nonExampleDetail("rest2")

        val page = Item.Page(
            details = listOf(rest1, example1, rest2, example2),
            nextPageTypes = nextPageTypes(),
        )

        val expected = listOf(
            page.copy(details = listOf(rest1, rest2)),
            page.copy(details = listOf(example1)),
            page.copy(details = listOf(example2)),
        )

        val result = useCase(page)
        assertEquals(expected, result)
    }

    private fun exampleDetail(text: String): LexicalItemDetail.Example =
        LexicalItemDetail.Example(
            TranslationsSet(
                original = Sentence(text, Lang.EN, DataSource.Kaikki),
                translations = emptyList(),
                translationsQualities = emptyList(),
            ),
            DataSource.Kaikki,
        )

    private fun nonExampleDetail(text: String): LexicalItemDetail =
        LexicalItemDetail.Synonyms(
            TranslationsSet(
                original = Sentence(text, Lang.EN, DataSource.Kaikki),
                translations = emptyList(),
                translationsQualities = emptyList(),
            ),
            DataSource.Kaikki,
        )

    private fun nextPageTypes(): Set<LexicalItemDetail.Type> =
        setOf(LexicalItemDetail.Type.entries.first())
}