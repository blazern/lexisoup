package blazern.lexisoup.feature.search_results

import arrow.core.Either.Right
import blazern.lexisoup.data.lexical_item_details_source.aggregation.LexicalItemDetailsSourceAggregator
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.FormsAccentsEnhancer
import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.FormsAccentsEnhancerProvider
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.LexicalItemDetail.Forms
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_MAX
import blazern.lexisoup.domain.model.WordForm
import blazern.lexisoup.feature.search_results.model.LexicalItemDetailsGroupState
import blazern.lexisoup.feature.search_results.model.priority
import blazern.lexisoup.feature.search_results.ui.SearchResultsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchResultsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val accentsEnhancer = object : FormsAccentsEnhancer {
        override fun enhance(sentence: Sentence) = sentence
    }

    private val accentsEnhancerProvider = object : FormsAccentsEnhancerProvider {
        override suspend fun provideFor(
            query: String,
            langFrom: Lang,
            langTo: Lang
        ) = Right(accentsEnhancer)
    }

    private fun List<LexicalItemDetailsSource>.aggregate() =
        LexicalItemDetailsSourceAggregator(this, accentsEnhancerProvider)


    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `received lexical items details sorting`() = runTest(testDispatcher) {
        val detailsMultiplier = 2
        val sources = fullSourcesWithFullDetails(detailsMultiplier = detailsMultiplier)
        val viewModel = SearchResultsViewModel(
            query = "query",
            langFrom = Lang.EN,
            langTo = Lang.DE,
            dataSource = sources.aggregate(),
        )

        while (true) {
            val loadings = viewModel.state.value.groups
                .filterIsInstance<LexicalItemDetailsGroupState.Loading>()
            if (loadings.isEmpty()) break
            loadings.forEach { viewModel.onLoadingDetailVisible(it) }
            advanceUntilIdle()
        }

        val sortedDataSources = List(detailsMultiplier) { DataSource.entries }
            .flatten()
            .sortedBy { it.priority }

        LexicalItemDetail.Type.entries.forEach { type ->
            val loadedForType = viewModel.state.value.groups
                .filterIsInstance<LexicalItemDetailsGroupState.Loaded>()
                .filter { type in it.types }

            assertEquals(
                sortedDataSources,
                loadedForType.map { it.source },
                loadedForType.toString(),
            )
        }
    }

    @Test
    fun `forms are filtered and sources with zero accepted forms are dropped`() =
        runTest(testDispatcher) {
            val sourceAccepted = DataSource.entries[0]
            val sourceRejected = DataSource.entries[1]

            val acceptedForms = listOf(
                WordForm("tanzt", emptyList(), Lang.DE),
                WordForm("die tanzen", emptyList(), Lang.DE),
                WordForm("haben or sein", listOf(WordForm.Tag.Defined.Auxiliary("auxiliary")), Lang.DE),
                WordForm("tanzt", emptyList(), Lang.DE),
            )
            val acceptedDetail = Forms(
                Forms.Value.Detailed(acceptedForms),
                sourceAccepted,
            )

            val rejectedForms = listOf(
                WordForm("habe getanzt", emptyList(), Lang.DE),
                WordForm("haben or sein", listOf(WordForm.Tag.Defined.Auxiliary("auxiliary")), Lang.DE),
                WordForm("der sehr tanzt", emptyList(), Lang.DE),
                WordForm("tanzt!", emptyList(), Lang.DE)
            )
            val rejectedDetail = Forms(
                Forms.Value.Detailed(rejectedForms),
                sourceRejected
            )

            val sources: List<LexicalItemDetailsSource> = listOf(
                FakeLexicalItemDetailsSource(sourceAccepted, listOf(acceptedDetail)),
                FakeLexicalItemDetailsSource(sourceRejected, listOf(rejectedDetail))
            )

            val vm = SearchResultsViewModel(
                query = "query",
                langFrom = Lang.EN,
                langTo = Lang.DE,
                dataSource = sources.aggregate(),
            )

            // Drive loading until idle
            while (true) {
                val loadings = vm.state.value.groups
                    .filterIsInstance<LexicalItemDetailsGroupState.Loading>()
                if (loadings.isEmpty()) break
                loadings.forEach { vm.onLoadingDetailVisible(it) }
                advanceUntilIdle()
            }

            val loadedFormGroups = vm.state.value.groups
                .filterIsInstance<LexicalItemDetailsGroupState.Loaded>()
                .filter { LexicalItemDetail.Type.FORMS in it.types }

            // Only the accepted source should remain (rejected one filtered out completely)
            assertEquals(listOf(sourceAccepted), loadedFormGroups.map { it.source })

            val loadedForms = loadedFormGroups.single().details.single() as Forms
            val value = loadedForms.value as Forms.Value.Detailed
            assertEquals(listOf("tanzt", "die tanzen"), value.forms.map { it.withoutPronoun().text })
        }

    private fun fullSourcesWithFullDetails(
        detailsMultiplier: Int = 1,
    ): List<FakeLexicalItemDetailsSource> {
        // Every type of DataSource
        val sources = DataSource.entries.map { source ->
            var details = mutableListOf<LexicalItemDetail>()
            details += List(detailsMultiplier) {
                Forms(
                    Forms.Value.Text("forms $detailsMultiplier $it"),
                    source,
                )
            }
            details += List(detailsMultiplier) {
                LexicalItemDetail.Explanation("Wörter $detailsMultiplier $it", source)
            }
            details += List(detailsMultiplier) {
                LexicalItemDetail.WordTranslations(
                    TranslationsSet(
                        original = Sentence("text $detailsMultiplier $it", Lang.EN, source),
                        translations = listOf(Sentence("Text", Lang.DE, source)),
                        translationsQualities = listOf(QUALITY_MAX),
                    ),
                    source,
                )
            }
            details += List(detailsMultiplier) {
                LexicalItemDetail.Synonyms(
                    TranslationsSet(
                        original = Sentence("text $detailsMultiplier $it", Lang.EN, source),
                        translations = listOf(Sentence("string", Lang.EN, source)),
                        translationsQualities = listOf(QUALITY_MAX),
                    ),
                    source,
                )
            }
            details += List(detailsMultiplier) {
                LexicalItemDetail.Example(
                    TranslationsSet(
                        original = Sentence("nice text $detailsMultiplier $it", Lang.EN, source),
                        translations = listOf(Sentence("schöner Text", Lang.DE, source)),
                        translationsQualities = listOf(QUALITY_MAX),
                    ),
                    source,
                )
            }
            // Make sure we didn't forget a type
            LexicalItemDetail.Type.entries.forEach { type ->
                assertTrue(details.any { it.type == type })
            }
            FakeLexicalItemDetailsSource(
                source = source,
                details = details,
            )
        }
        return sources
    }
}

private class FakeLexicalItemDetailsSource(
    override val source: DataSource,
    val details: List<LexicalItemDetail>,
) : LexicalItemDetailsSource {
    override val types = LexicalItemDetail.Type.entries.toSet()

    override fun request(
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ): Flow<Item> = flow {
        details.forEach {
            val page = Item.Page(
                details = listOf(it),
                nextPageTypes = types,
            )
            emit(page)
        }
    }
}
