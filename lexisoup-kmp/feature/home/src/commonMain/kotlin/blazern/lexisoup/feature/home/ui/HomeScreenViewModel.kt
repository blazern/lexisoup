package blazern.lexisoup.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import blazern.lexisoup.core.logging.Log
import blazern.lexisoup.data.suggestions.SuggestionsProvider
import blazern.lexisoup.domain.backend_address.BackendAddressProvider
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.Suggestion
import blazern.lexisoup.domain.settings.SettingsRepository
import blazern.lexisoup.feature.home.SearchFn
import blazern.lexisoup.feature.home.model.HomeScreenState
import blazern.lexisoup.utils.KotlinPlatform
import blazern.lexisoup.utils.getKotlinPlatform
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal class HomeScreenViewModel(
    query: String,
    private val settings: SettingsRepository,
    private val backendAddressProvider: BackendAddressProvider,
    private val suggestionsProvider: SuggestionsProvider,
) : ViewModel() {
    private val localhostAllowed = getKotlinPlatform() == KotlinPlatform.JS

    private val _state = MutableStateFlow(HomeScreenState(
        langFrom = null,
        langTo = null,
        query = query,
        canSearch = canSearch(query),
        suggestions = emptyList(),
        suggestionsTarget = "",
        isLocalhost = if (localhostAllowed) false else null,
    ))
    val state: StateFlow<HomeScreenState> = _state

    init {
        settings.getLangFrom().onEach { langFrom ->
            _state.update { it.copy(langFrom = langFrom) }
        }.launchIn(viewModelScope)

        settings.getLangTo().onEach { langTo ->
            _state.update { it.copy(langTo = langTo) }
        }.launchIn(viewModelScope)

        @OptIn(ExperimentalCoroutinesApi::class)
        _state
            .map { Triple(it.query, it.langFrom, it.langTo) }
            .distinctUntilChanged()
            .transformLatest { (query, langFrom, langTo) ->
                val canRequest = state.value.canSearch
                        && langFrom != null
                        && langTo != null

                if (!canRequest) {
                    emit(emptyList<Suggestion>() to "")
                    return@transformLatest
                }

                // if query changes, the call after the [delay] gets cancelled
                delay(DELAY_BEFORE_SUGGESTION_REQUEST)

                var suggestions = suggestionsProvider.suggestionsFor(query, langFrom, langTo)
                    .fold(
                        {
                            Log.e(TAG, it.e) { "Error while retrieving suggestions" }
                            emit(emptyList<Suggestion>() to query)
                            return@transformLatest
                        },
                        { it }
                    )
                if (suggestions.isNotEmpty()) {
                    emit(suggestions to query)
                    return@transformLatest
                }

                // Maybe the user forgot to switch languages?
                suggestions = suggestionsProvider.suggestionsFor(
                    query,
                    langFrom = langTo,
                    langTo = langFrom,
                )
                    .fold(
                        {
                            Log.e(TAG, it.e) { "Error while retrieving reverse suggestions" }
                            emptyList()
                        },
                        { it }
                    )
                emit(suggestions to query)
            }.onEach { (suggestions, query) ->
                _state.update {
                    it.copy(
                        suggestions = suggestions,
                        suggestionsTarget = query,
                    )
                }
            }.launchIn(viewModelScope)

        if (localhostAllowed) {
            backendAddressProvider.isLocalhost.onEach { isLocalhost ->
                _state.update { it.copy(isLocalhost = isLocalhost) }
            }.launchIn(viewModelScope)
        }
    }

    fun onQueryChange(value: String) {
        _state.update {
            it.copy(
                query = value,
                canSearch = canSearch(value),
            )
        }
    }

    fun onLangsChange(langFrom: Lang, langTo: Lang) {
        viewModelScope.launch {
            val oldLangTo = requireNotNull(_state.value.langTo)
            val oldLangFrom = requireNotNull(_state.value.langFrom)
            if (langFrom == langTo) {
                settings.setLangTo(oldLangFrom)
                settings.setLangFrom(oldLangTo)
            } else {
                settings.setLangTo(langTo)
                settings.setLangFrom(langFrom)
            }
        }
    }

    fun onLocalhostToggled(enabled: Boolean) {
        viewModelScope.launch {
            backendAddressProvider.setIsLocalhost(enabled)
        }
    }

    fun onSuggestionClick(
        suggestion: Suggestion,
        searchFn: SearchFn,
    ) {
        val langFrom = state.value.langFrom ?: return
        val langTo = state.value.langTo ?: return
        if (suggestion.lang == state.value.langFrom) {
            searchFn(suggestion.text, langFrom, langTo)
        } else {
            onLangsChange(langFrom = langTo, langTo = langFrom)
            searchFn(suggestion.text, langTo, langFrom)
        }
    }

    // Visible for tests
    internal fun canSearch(query: String): Boolean {
        return MIN_QUERY_LENGTH <= query.trim().length
    }

    companion object {
        private const val TAG = "HomeScreenViewModel"
        // Visible for tests
        internal const val MIN_QUERY_LENGTH = 2
        internal val DELAY_BEFORE_SUGGESTION_REQUEST = 300.milliseconds
    }
}
