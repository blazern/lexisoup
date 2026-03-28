package blazern.lexisoup.feature.home.ui

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import androidx.lifecycle.ViewModel
import blazern.lexisoup.data.suggestions.SuggestionsProvider
import blazern.lexisoup.domain.backend_address.BackendAddressProvider
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.Suggestion
import blazern.lexisoup.domain.settings.SettingsRepository
import blazern.lexisoup.feature.home.model.HomeScreenState
import blazern.lexisoup.feature.home.ui.HomeScreenViewModel.Companion.DELAY_BEFORE_SUGGESTION_REQUEST
import blazern.lexisoup.feature.home.ui.HomeScreenViewModel.Companion.MIN_QUERY_LENGTH
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class HomeScreenViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `langFrom and langTo from settings are propagated to state`() = runTest(testDispatcher) {
        val settings = FakeSettingsRepository(
            langFrom = Lang.EN,
            langTo = Lang.DE,
        )
        val viewModel = createViewModel(
            query = "",
            settings = settings,
        )

        advanceUntilIdle()

        assertEquals(Lang.EN, viewModel.state.value.langFrom)
        assertEquals(Lang.DE, viewModel.state.value.langTo)
    }

    @Test
    fun `suggestions with good and short query`() = runTest(testDispatcher) {
        val suggestions = listOf(
            Suggestion(text = "cat", lang = Lang.EN, translations = emptyList()),
        )
        val viewModel = createViewModel(
            query = "c".repeat(MIN_QUERY_LENGTH),
            suggestionsProvider = FakeSuggestionsProvider { query, langFrom, langTo ->
                Right(suggestions)
            }
        )

        advanceTimeBy(DELAY_BEFORE_SUGGESTION_REQUEST)
        advanceUntilIdle()

        // The query was long enough
        assertEquals(suggestions, viewModel.state.value.suggestions)
        assertEquals("c".repeat(MIN_QUERY_LENGTH), viewModel.state.value.suggestionsTarget)

        viewModel.onQueryChange("c".repeat(MIN_QUERY_LENGTH - 1))
        advanceUntilIdle()

        // The query was too short
        assertEquals(emptyList(), viewModel.state.value.suggestions)
        assertEquals("", viewModel.state.value.suggestionsTarget)
    }

    @Test
    fun `suggestions request is delayed and cancelled on query change`() = runTest(testDispatcher) {
        val providerCalls = mutableListOf<Triple<String, Lang, Lang>>()
        val viewModel = createViewModel(
            query = "",
            suggestionsProvider = FakeSuggestionsProvider { query, langFrom, langTo ->
                providerCalls += Triple(query, langFrom, langTo)
                Right(listOf(Suggestion(text = "cat", lang = Lang.EN, translations = emptyList())))
            }
        )

        advanceUntilIdle()

        // Query long enough, but the timer is advanced not long enough into the future
        viewModel.onQueryChange("c".repeat(MIN_QUERY_LENGTH))
        advanceTimeBy(DELAY_BEFORE_SUGGESTION_REQUEST - 1.milliseconds)
        assertEquals(emptyList(), providerCalls)

        // Query is long enough again, the timer is not advanced enough again
        viewModel.onQueryChange("c".repeat(MIN_QUERY_LENGTH + 1))
        advanceTimeBy(DELAY_BEFORE_SUGGESTION_REQUEST - 1.milliseconds)
        assertEquals(emptyList(), providerCalls)

        // And now the timer is advanced long enough finally
        advanceTimeBy(2.milliseconds)

        assertEquals(
            listOf(Triple("c".repeat(MIN_QUERY_LENGTH + 1), Lang.EN, Lang.DE)),
            providerCalls,
        )
    }

    @Test
    fun `non-empty direct suggestions`() = runTest(testDispatcher) {
        val expectedSuggestions = listOf(
            Suggestion(text = "cat", lang = Lang.EN, emptyList()),
            Suggestion(text = "catalog", lang = Lang.EN, emptyList()),
        )
        val providerCalls = mutableListOf<Triple<String, Lang, Lang>>()
        val viewModel = createViewModel(
            query = "cat",
            suggestionsProvider = FakeSuggestionsProvider { query, langFrom, langTo ->
                providerCalls += Triple(query, langFrom, langTo)
                Right(expectedSuggestions)
            }
        )

        advanceTimeBy(DELAY_BEFORE_SUGGESTION_REQUEST + 1.milliseconds)

        assertEquals(
            listOf(Triple("cat", Lang.EN, Lang.DE)),
            providerCalls,
        )
        assertEquals(expectedSuggestions, viewModel.state.value.suggestions)
        assertEquals("cat", viewModel.state.value.suggestionsTarget)
    }

    @Test
    fun `reverse lang suggestions lookup`() = runTest(testDispatcher) {
        val expectedSuggestions = listOf(
            Suggestion(text = "Katze", lang = Lang.DE, emptyList()),
        )
        val providerCalls = mutableListOf<Triple<String, Lang, Lang>>()
        val viewModel = createViewModel(
            query = "cat",
            suggestionsProvider = FakeSuggestionsProvider { query, langFrom, langTo ->
                providerCalls += Triple(query, langFrom, langTo)
                when (Triple(query, langFrom, langTo)) {
                    // No suggestions for EN -> DE
                    Triple("cat", Lang.EN, Lang.DE) -> Right(emptyList())
                    // Suggestions for DE -> EN only
                    Triple("cat", Lang.DE, Lang.EN) -> Right(expectedSuggestions)
                    else -> error("Unexpected request")
                }
            }
        )

        advanceTimeBy(DELAY_BEFORE_SUGGESTION_REQUEST + 1.milliseconds)

        assertEquals(
            listOf(
                Triple("cat", Lang.EN, Lang.DE),
                Triple("cat", Lang.DE, Lang.EN),
            ),
            providerCalls,
        )
        assertEquals(expectedSuggestions, viewModel.state.value.suggestions)
        assertEquals("cat", viewModel.state.value.suggestionsTarget)
    }

    @Test
    fun `suggestions errors result in no suggestions`() = runTest(testDispatcher) {
        val providerCalls = mutableListOf<Triple<String, Lang, Lang>>()
        val viewModel = createViewModel(
            query = "cat",
            suggestionsProvider = FakeSuggestionsProvider { query, langFrom, langTo ->
                providerCalls += Triple(query, langFrom, langTo)
                Left(Err.from(Exception("boom")))
            }
        )

        advanceTimeBy(DELAY_BEFORE_SUGGESTION_REQUEST + 1.milliseconds)

        assertEquals(
            listOf(
                Triple("cat", Lang.EN, Lang.DE),
                // NOTE: no reversed request
            ),
            providerCalls,
        )
        assertEquals(emptyList(), viewModel.state.value.suggestions)
        assertEquals("cat", viewModel.state.value.suggestionsTarget)
    }

    @Test
    fun `onQueryChange updates query and canSearch`() = runTest(testDispatcher) {
        val viewModel = createViewModel(query = "")

        viewModel.onQueryChange("c".repeat(MIN_QUERY_LENGTH - 1))
        assertEquals("c".repeat(MIN_QUERY_LENGTH - 1), viewModel.state.value.query)
        assertEquals(false, viewModel.state.value.canSearch)


        viewModel.onQueryChange("c".repeat(MIN_QUERY_LENGTH))
        assertEquals("c".repeat(MIN_QUERY_LENGTH), viewModel.state.value.query)
        assertEquals(true, viewModel.state.value.canSearch)
    }

    @Test
    fun `onLangsChange updates settings and swaps when equal langs are passed`() = runTest(testDispatcher) {
        val settings = FakeSettingsRepository(
            langFrom = Lang.EN,
            langTo = Lang.DE,
        )
        val viewModel = createViewModel(
            query = "",
            settings = settings,
        )
        advanceUntilIdle()

        viewModel.onLangsChange(langFrom = Lang.FR, langTo = Lang.RU)
        advanceUntilIdle()
        assertEquals(Lang.FR, viewModel.state.value.langFrom)
        assertEquals(Lang.RU, viewModel.state.value.langTo)

        viewModel.onLangsChange(langFrom = Lang.RU, langTo = Lang.RU)
        advanceUntilIdle()
        assertEquals(Lang.RU, viewModel.state.value.langFrom)
        assertEquals(Lang.FR, viewModel.state.value.langTo)
    }

    @Test
    fun `onSuggestionClick performs search`() = runTest(testDispatcher) {
        val settings = FakeSettingsRepository(
            langFrom = Lang.EN,
            langTo = Lang.DE,
        )
        val viewModel = createViewModel(
            query = "",
            settings = settings,
        )
        val searchCalls = mutableListOf<Triple<String, Lang, Lang>>()

        advanceUntilIdle()

        viewModel.onSuggestionClick(
            suggestion = Suggestion(text = "cat", lang = Lang.EN, emptyList()),
        ) { query, langFrom, langTo ->
            searchCalls += Triple(query, langFrom, langTo)
        }

        advanceUntilIdle()

        assertEquals(
            listOf(Triple("cat", Lang.EN, Lang.DE)),
            searchCalls,
        )
    }

    @Test
    fun `onSuggestionClick with suggestion in reversed lang`() = runTest(testDispatcher) {
        val settings = FakeSettingsRepository(
            langFrom = Lang.EN,
            langTo = Lang.DE,
        )
        val viewModel = createViewModel(
            query = "",
            settings = settings,
        )
        val searchCalls = mutableListOf<Triple<String, Lang, Lang>>()

        advanceUntilIdle()

        viewModel.onSuggestionClick(
            suggestion = Suggestion(text = "Katze", lang = Lang.DE, emptyList()),
        ) { query, langFrom, langTo ->
            searchCalls += Triple(query, langFrom, langTo)
        }

        advanceUntilIdle()

        assertEquals(
            listOf(Triple("Katze", Lang.DE, Lang.EN)),
            searchCalls,
        )
        // Langs in the settings are now also switched
        assertEquals(Lang.DE, viewModel.state.value.langFrom)
        assertEquals(Lang.EN, viewModel.state.value.langTo)
    }

    private fun createViewModel(
        query: String,
        settings: FakeSettingsRepository = FakeSettingsRepository(
            langFrom = Lang.EN,
            langTo = Lang.DE,
        ),
        backendAddressProvider: BackendAddressProvider = FakeBackendAddressProvider(),
        suggestionsProvider: SuggestionsProvider = FakeSuggestionsProvider { _, _, _ ->
            Right(emptyList())
        },
    ): HomeScreenViewModel {
        return HomeScreenViewModel(
            query = query,
            settings = settings,
            backendAddressProvider = backendAddressProvider,
            suggestionsProvider = suggestionsProvider,
        )
    }
}

private class FakeSettingsRepository(
    langFrom: Lang,
    langTo: Lang,
) : SettingsRepository {
    private val langFromFlow = MutableStateFlow(langFrom)
    private val langToFlow = MutableStateFlow(langTo)

    override suspend fun setLangFrom(lang: Lang) {
        langFromFlow.value = lang
    }

    override fun getLangFrom(): Flow<Lang> = langFromFlow

    override suspend fun setLangTo(lang: Lang) {
        langToFlow.value = lang
    }

    override fun getLangTo(): Flow<Lang> = langToFlow
}

private class FakeBackendAddressProvider : BackendAddressProvider {
    override val baseUrl: Flow<String> = emptyFlow()
    override val isLocalhost: Flow<Boolean> = emptyFlow()

    override suspend fun setIsLocalhost(isLocalhost: Boolean) {
        error("not implemented")
    }
}

private class FakeSuggestionsProvider(
    private val suggestionsForFn: suspend (
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ) -> Either<Err, List<Suggestion>>,
) : SuggestionsProvider {
    override suspend fun suggestionsFor(
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ): Either<Err, List<Suggestion>> {
        return suggestionsForFn(query, langFrom, langTo)
    }
}
