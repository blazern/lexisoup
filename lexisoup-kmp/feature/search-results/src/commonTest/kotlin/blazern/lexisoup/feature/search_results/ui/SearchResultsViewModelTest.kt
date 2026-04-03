package blazern.lexisoup.feature.search_results.ui

import arrow.core.Either.Right
import blazern.lexisoup.data.lexical_item_details_source.aggregation.LexicalItemDetailsSourceAggregator
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.FormsAccentsEnhancer
import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.FormsAccentsEnhancerProvider
import blazern.lexisoup.domain.analytics.Analytics
import blazern.lexisoup.domain.analytics.Event
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.LexicalItemDetail.Forms
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_MAX
import blazern.lexisoup.domain.model.predefined
import blazern.lexisoup.domain.settings.SettingsRepository
import blazern.lexisoup.feature.search_results.FakeCreateTranslationsStatesUseCase
import blazern.lexisoup.feature.search_results.FakeTransformPageUseCase
import blazern.lexisoup.feature.search_results.FakeTranslateDetailsUseCase
import blazern.lexisoup.feature.search_results.FakeTranslator
import blazern.lexisoup.feature.search_results.FakeTranslatorsAggregator
import blazern.lexisoup.feature.search_results.model.LexicalItemDetailsGroupState
import blazern.lexisoup.feature.search_results.model.TranslationState
import blazern.lexisoup.feature.search_results.model.priority
import blazern.lexisoup.feature.search_results.repository.BackgroundErrorsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
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

    private val errorsRepo = BackgroundErrorsRepository()

    private fun List<LexicalItemDetailsSource>.aggregate() = LexicalItemDetailsSourceAggregator(
        this,
        accentsEnhancerProvider,
        FakeSettingsRepository(),
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        startKoin {}
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
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
            analytics = FakeAnalytics(),
            dataSource = sources.aggregate(),
            translators = FakeTranslatorsAggregator(listOf(FakeTranslator())),
            translateDetails = FakeTranslateDetailsUseCase { _, _, _, _ -> emptyFlow() },
            transformPage = FakeTransformPageUseCase { page -> listOf(page) },
            createTranslationsStates = FakeCreateTranslationsStatesUseCase { _, _, _ -> emptyList() },
            errorsRepo = errorsRepo,
        )

        loadEverything(viewModel)

        val sortedDataSources = List(detailsMultiplier) { DataSource.predefined }
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
    fun `initial translation states are created and assigned to loaded groups`() = runTest(testDispatcher) {
        val expectedTranslationStates = listOf(
            TranslationState.CanStart(DataSource.DeepL),
            TranslationState.CanStart(DataSource.ChatGPT),
        )

        val sources = fullSourcesWithFullDetails(detailsMultiplier = 1)
        val viewModel = SearchResultsViewModel(
            query = "query",
            langFrom = Lang.EN,
            langTo = Lang.DE,
            analytics = FakeAnalytics(),
            dataSource = sources.aggregate(),
            translators = FakeTranslatorsAggregator(
                listOf(
                    FakeTranslator(source = DataSource.DeepL),
                    FakeTranslator(source = DataSource.ChatGPT),
                )
            ),
            translateDetails = FakeTranslateDetailsUseCase { _, _, _, _ -> emptyFlow() },
            transformPage = FakeTransformPageUseCase { page -> listOf(page) },
            createTranslationsStates = FakeCreateTranslationsStatesUseCase { _, _, _ ->
                expectedTranslationStates
            },
            errorsRepo = errorsRepo,
        )

        loadEverything(viewModel)

        val loadedGroups = viewModel.state.value.groups
            .filterIsInstance<LexicalItemDetailsGroupState.Loaded>()

        assertTrue(loadedGroups.isNotEmpty(), "Expected at least one Loaded group")

        loadedGroups.forEach { loaded ->
            assertEquals(
                expectedTranslationStates,
                loaded.translationStates,
            )
        }
    }

    @Test
    fun `onTranslateRequest immediately marks selected source as InProgress`() = runTest(testDispatcher) {
        val translationSource = DataSource.DeepL
        val initialStates = listOf(
            TranslationState.CanStart(translationSource),
            TranslationState.CanStart(DataSource.ChatGPT),
        )

        val sources = fullSourcesWithFullDetails(detailsMultiplier = 1)
        val viewModel = SearchResultsViewModel(
            query = "query",
            langFrom = Lang.EN,
            langTo = Lang.DE,
            analytics = FakeAnalytics(),
            dataSource = sources.aggregate(),
            translators = FakeTranslatorsAggregator(
                translators = listOf(
                    FakeTranslator(source = translationSource),
                    FakeTranslator(source = DataSource.ChatGPT),
                )
            ),
            translateDetails = FakeTranslateDetailsUseCase { _, _, _, _ ->
                // Never complete
                flow { awaitCancellation() }
            },
            transformPage = FakeTransformPageUseCase { page -> listOf(page) },
            createTranslationsStates = FakeCreateTranslationsStatesUseCase { _, _, _ -> initialStates },
            errorsRepo = errorsRepo,
        )

        loadEverything(viewModel)
        val group = viewModel.firstLoadedGroup()

        viewModel.onTranslateRequest(group, translationSource)

        val updated = viewModel.loadedById(group.id)
        val expected = group.translationStates.map {
            if (it.translationSource == translationSource) {
                TranslationState.InProgress(translationSource)
            } else it
        }
        assertEquals(expected, updated.translationStates)
    }

    @Test
    fun `onTranslateRequest works`() = runTest(testDispatcher) {
        val allSources = fullSourcesWithFullDetails()
        val translationSource = DataSource.DeepL
        val translations = fullDetails(translationSource, textsPostfix = "TRANSLATED")
            .associateBy { it.type }

        val fakeTranslator = FakeTranslator(source = translationSource)
        var createTranslationsStatesCallsCount = 0
        val viewModel = SearchResultsViewModel(
            query = "query",
            langFrom = Lang.EN,
            langTo = Lang.DE,
            analytics = FakeAnalytics(),
            dataSource = allSources.aggregate(),
            translators = FakeTranslatorsAggregator(listOf(fakeTranslator)),
            translateDetails = FakeTranslateDetailsUseCase { details, _, _, _ ->
                details.map { Right(translations[it.type]!!) }.asFlow()
            },
            transformPage = FakeTransformPageUseCase { page -> listOf(page) },
            createTranslationsStates = FakeCreateTranslationsStatesUseCase { _, _, _ ->
                createTranslationsStatesCallsCount += 1
                emptyList()
            },
            errorsRepo = errorsRepo,
        )

        loadEverything(viewModel)
        val group = viewModel.firstLoadedGroup()

        val createTranslationsStatesCallsCount1 = createTranslationsStatesCallsCount
        viewModel.onTranslateRequest(group, translationSource)
        advanceUntilIdle()
        assertTrue(
            createTranslationsStatesCallsCount1 < createTranslationsStatesCallsCount,
            createTranslationsStatesCallsCount.toString(),
        )

        val translatedGroup = viewModel.loadedById(group.id)
        assertEquals(
            group.details.map { translations[it.type]!! },
            translatedGroup.details,
        )
    }

    @Test
    fun `translation states are recalculated after langs download`() = runTest(testDispatcher) {
        val translationSource = DataSource.DeepL
        var languagePackDownloaded = false

        val source = FakeLexicalItemDetailsSource(
            source = DataSource.Kaikki,
            details = listOf(
                LexicalItemDetail.Explanation(
                    Sentence("first", Lang.EN, DataSource.Kaikki),
                    DataSource.Kaikki,
                ),
                LexicalItemDetail.Explanation(
                    Sentence("second", Lang.EN, DataSource.Kaikki),
                    DataSource.Kaikki,
                ),
            ),
        )

        val viewModel = SearchResultsViewModel(
            query = "query",
            langFrom = Lang.EN,
            langTo = Lang.DE,
            analytics = FakeAnalytics(),
            dataSource = listOf(source).aggregate(),
            translators = FakeTranslatorsAggregator(
                listOf(FakeTranslator(source = translationSource))
            ),
            translateDetails = FakeTranslateDetailsUseCase { details, _, _, _ ->
                languagePackDownloaded = true
                details.map { Right(it) }.asFlow()
            },
            transformPage = FakeTransformPageUseCase { page -> listOf(page) },
            createTranslationsStates = FakeCreateTranslationsStatesUseCase { _, _, _ ->
                listOf(
                    if (languagePackDownloaded) {
                        TranslationState.CanStart(translationSource)
                    } else {
                        TranslationState.MustDownloadLangs(translationSource)
                    }
                )
            },
            errorsRepo = errorsRepo,
        )

        loadEverything(viewModel)

        val loadedGroupsBefore = viewModel.state.value.groups
            .filterIsInstance<LexicalItemDetailsGroupState.Loaded>()

        assertEquals(2, loadedGroupsBefore.size)
        assertEquals(
            listOf(TranslationState.MustDownloadLangs(translationSource)),
            loadedGroupsBefore[0].translationStates,
        )
        assertEquals(
            listOf(TranslationState.MustDownloadLangs(translationSource)),
            loadedGroupsBefore[1].translationStates,
        )

        viewModel.onTranslateRequest(loadedGroupsBefore[0], translationSource)
        advanceUntilIdle()

        val loadedGroupsAfter = viewModel.state.value.groups
            .filterIsInstance<LexicalItemDetailsGroupState.Loaded>()

        assertEquals(
            listOf(TranslationState.CanStart(translationSource)),
            loadedGroupsAfter[0].translationStates,
        )
        assertEquals(
            listOf(TranslationState.CanStart(translationSource)),
            loadedGroupsAfter[1].translationStates,
        )
    }

    private fun fullSourcesWithFullDetails(
        detailsMultiplier: Int = 1,
        textsPostfix: String = "",
    ): List<FakeLexicalItemDetailsSource> {
        // Every type of DataSource
        val sources = DataSource.predefined.map { source ->
            val details = fullDetails(source, detailsMultiplier, textsPostfix)
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

    private fun fullDetails(
        source: DataSource,
        detailsMultiplier: Int = 1,
        textsPostfix: String = "",
    ): List<LexicalItemDetail> {
        // Every type of DataSource
        var details = mutableListOf<LexicalItemDetail>()
        details += List(detailsMultiplier) {
            Forms(
                Forms.Value.Text("forms $detailsMultiplier $it $textsPostfix"),
                Lang.DE,
                source,
            )
        }
        details += List(detailsMultiplier) {
            LexicalItemDetail.Explanation(
                Sentence("Wörter $detailsMultiplier $it $textsPostfix", Lang.DE, source),
                source,
            )
        }
        details += List(detailsMultiplier) {
            LexicalItemDetail.WordTranslations(
                TranslationsSet(
                    original = Sentence("text $detailsMultiplier $it $textsPostfix", Lang.EN, source),
                    translations = listOf(Sentence("Text", Lang.DE, source)),
                    translationsQualities = listOf(QUALITY_MAX),
                ),
                source,
            )
        }
        details += List(detailsMultiplier) {
            LexicalItemDetail.Synonyms(
                listOf(Sentence("string $textsPostfix", Lang.EN, source)),
                source,
            )
        }
        details += List(detailsMultiplier) {
            LexicalItemDetail.Example(
                TranslationsSet(
                    original = Sentence("nice text $detailsMultiplier $it $textsPostfix", Lang.EN, source),
                    translations = listOf(Sentence("schöner Text $textsPostfix", Lang.DE, source)),
                    translationsQualities = listOf(QUALITY_MAX),
                ),
                source,
            )
        }
        details += List(detailsMultiplier) {
            LexicalItemDetail.Etymology(
                TranslationsSet(
                    original = Sentence("nice text $detailsMultiplier $it $textsPostfix", Lang.EN, source),
                ),
                source,
            )
        }
        details += List(detailsMultiplier) {
            LexicalItemDetail.Pronunciation.Audio(
                "Some audio",
                listOf("https://upload.wikimedia.org/wikipedia/commons/transcoded/4/40/De-Katze.ogg/De-Katze.ogg.mp3"),
                source,
            )
        }
        return details
    }

    private fun TestScope.loadEverything(viewModel: SearchResultsViewModel) {
        while (true) {
            val loadings = viewModel.state.value.groups
                .filterIsInstance<LexicalItemDetailsGroupState.Loading>()
            if (loadings.isEmpty()) break
            loadings.forEach(viewModel::onLoadingDetailVisible)
            advanceUntilIdle()
        }
    }

    private fun SearchResultsViewModel.firstLoadedGroup(): LexicalItemDetailsGroupState.Loaded {
        val loaded = state.value.groups.filterIsInstance<LexicalItemDetailsGroupState.Loaded>()
        assertTrue(loaded.isNotEmpty(), "Expected at least one Loaded group")
        return loaded.first()
    }

    private fun SearchResultsViewModel.loadedById(id: String): LexicalItemDetailsGroupState.Loaded =
        state.value.groups
            .filterIsInstance<LexicalItemDetailsGroupState.Loaded>()
            .first { it.id == id }
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

private class FakeSettingsRepository : SettingsRepository {
    override fun getExcludedDataSourcesIDs(): Flow<Set<String>> = flowOf(emptySet())
    override fun getExcludedLexicalItemsDetailsTypes(): Flow<Set<LexicalItemDetail.Type>> =
        flowOf(emptySet())
}

private class FakeAnalytics : Analytics {
    override fun log(event: Event) = Unit
}
