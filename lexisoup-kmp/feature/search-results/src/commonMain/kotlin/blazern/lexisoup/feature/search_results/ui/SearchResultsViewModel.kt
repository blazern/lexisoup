package blazern.lexisoup.feature.search_results.ui

import androidx.compose.ui.platform.Clipboard
import androidx.lifecycle.viewModelScope
import arrow.core.getOrElse
import blazern.lexisoup.core.logging.Log
import blazern.lexisoup.data.lexical_item_details_source.aggregation.LexicalItemDetailsSourceAggregator
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.data.translator.aggregation.TranslatorsAggregator
import blazern.lexisoup.domain.analytics.Analytics
import blazern.lexisoup.domain.analytics.Event
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.predefined
import blazern.lexisoup.feature.search_results.model.LexicalItemDetailsGroupState
import blazern.lexisoup.feature.search_results.model.SearchResultsState
import blazern.lexisoup.feature.search_results.model.TranslationState
import blazern.lexisoup.feature.search_results.model.addLoadingFor
import blazern.lexisoup.feature.search_results.model.removeAllButLoadedFor
import blazern.lexisoup.feature.search_results.model.removeErrorsFor
import blazern.lexisoup.feature.search_results.model.replaceByID
import blazern.lexisoup.feature.search_results.model.replaceMatchingOrJustAdd
import blazern.lexisoup.feature.search_results.repository.BackgroundErrorsRepository
import blazern.lexisoup.feature.search_results.usecases.CreateTranslationsStatesUseCase
import blazern.lexisoup.feature.search_results.usecases.TransformPageUseCase
import blazern.lexisoup.feature.search_results.usecases.TranslateDetailsUseCase
import blazern.lexisoup.utils.FlowIterator
import blazern.lexisoup.utils.clipEntryOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.viewmodel.scope.ScopeViewModel

private typealias LexicalItemDetailFlow = FlowIterator<Item>

@OptIn(KoinExperimentalAPI::class)
@Suppress("LongParameterList")
internal class SearchResultsViewModel(
    private val query: String,
    private val langFrom: Lang,
    private val langTo: Lang,
    private val analytics: Analytics,
    dataSource: LexicalItemDetailsSourceAggregator,
    private val translators: TranslatorsAggregator,
    private val errorsRepo: BackgroundErrorsRepository,
    private val translateDetails: TranslateDetailsUseCase,
    private val transformPage: TransformPageUseCase,
    private val createTranslationsStates: CreateTranslationsStatesUseCase,
) : ScopeViewModel() {
    private val loadingInProgress = mutableSetOf<DataSource>()
    private val dataIters = mutableMapOf<DataSource, LexicalItemDetailFlow>()
    private val sourceTypes = mutableMapOf<DataSource, Set<LexicalItemDetail.Type>>()
    // NOTE: if new fields and responsibilities are being added, extract the 3 fields above and
    // a ton of related methods into a separate class.

    private val dataSource = dataSource.sourceFor(query, langFrom, langTo)

    private val _state = MutableStateFlow(SearchResultsState())
    val state: StateFlow<SearchResultsState> = _state.asStateFlow()

    init {
        viewModelScope.launch { search() }
        state
            .onEach { Log.d(TAG) {
                @Suppress("MagicNumber")
                "State:${it.toLogsString().take(256)}" }
            }
            .shareIn(viewModelScope, SharingStarted.Eagerly)
    }

    private suspend fun search() {
        analytics.log(Event.Search(query, langFrom, langTo))
        _state.update { SearchResultsState() }
        for (source in DataSource.predefined) {
            sourceTypes[source] = dataSource.typesOf(source)
            val flow = dataSource.request(source)
            dataIters[source] = FlowIterator(flow)
            continueLoadingFor(source)
        }
    }

    fun copyText(text: String, clipboard: Clipboard) {
        viewModelScope.launch {
            clipboard.setClipEntry(
                clipEntryOf(text)
            )
        }
    }

    fun onLoadingDetailVisible(
        loading: LexicalItemDetailsGroupState.Loading,
    ) {
        continueLoadingFor(loading.source)
    }

    private fun continueLoadingFor(
        source: DataSource,
    ) {
        val iter = dataIters[source] ?: return
        viewModelScope.launch {
            Log.d(TAG) {
                "continueLoadingFor: $source, " +
                "in progress: ${loadingInProgress.contains(source)}, " +
                "has ended: ${iter.hasEnded()}"
            }
            if (loadingInProgress.contains(source) || iter.hasEnded()) {
                return@launch
            }
            loadingInProgress.add(source)
            val sourceTypes = sourceTypes[source].orEmpty()
            _state.update { state ->
                state
                    .removeAllButLoadedFor(source)
                    .addLoadingFor(source, sourceTypes)
            }
            val next = iter.next()
            if (next != null) {
                onNextDetailResult(next, source, sourceTypes)
            } else {
                // The end
                _state.update { it.removeAllButLoadedFor(source) }
            }
            loadingInProgress.remove(source)
        }
    }

    private suspend fun onNextDetailResult(
        item: Item,
        source: DataSource,
        requestedTypes: Set<LexicalItemDetail.Type>,
    ) {
        when (item) {
            is Item.Failure -> {
                _state.update {
                    it.replaceMatchingOrJustAdd(
                        item,
                        source,
                        requestedTypes,
                    ) {
                        it.source == source && it !is LexicalItemDetailsGroupState.Loaded
                    }
                }
            }
            is Item.Page -> {
                val pages = transformPage(item)
                pages.forEach { page ->
                    val translationsStates = createTranslationsStates(page.details, langFrom, langTo)
                    _state.update {
                        it.replaceMatchingOrJustAdd(
                            page,
                            source,
                            translationsStates,
                        ) {
                            it.source == source && it !is LexicalItemDetailsGroupState.Loaded
                        }
                    }
                }
                _state.update { it.addLoadingFor(source, item.nextPageTypes) }
                sourceTypes[source] = item.nextPageTypes
            }
        }
    }

    fun onFixErrorRequest(error: LexicalItemDetailsGroupState.Error) {
        _state.update { it.removeErrorsFor(error.source) }
        continueLoadingFor(error.source)
    }

    fun onTranslateRequest(
        detailsGroup: LexicalItemDetailsGroupState.Loaded,
        translationSource: DataSource,
    ) {
        val inProgress = detailsGroup.copy(translationStates = detailsGroup.translationStates.map {
            if (it.translationSource == translationSource) {
                TranslationState.InProgress(translationSource)
            } else {
                it
            }
        })
        _state.update { it.replaceByID(inProgress) }

        viewModelScope.launch {
            val translator = translators.getTranslator(translationSource)

            val translationsResults = translateDetails(
                detailsGroup.details,
                translator,
                langFrom,
                langTo,
            ).toList()
            val errors = translationsResults.mapNotNull { it.leftOrNull() }
            val detailsTranslated = if (errors.isNotEmpty()) {
                errors.forEach { errorsRepo.emit(it) }
                // Let's consider nothing to be translated
                detailsGroup.details
            } else {
                translationsResults.map {
                    it.getOrElse { err ->
                        throw IllegalStateException("Expected no errors", err.e)
                    }
                }
            }

            _state.update { it.replaceByID(detailsGroup.copy(details = detailsTranslated)) }
            // Let's recalculate the translations' states for each of the loaded groups,
            // because it's possible current translation has downloaded a language pack
            // See implementation of [TranslateDetailsUseCase].
            state.value.groups.forEach { group ->
                val updatedGroup = when (group) {
                    is LexicalItemDetailsGroupState.Loaded -> group.copy(
                        translationStates = createTranslationsStates(group.details, langFrom, langTo)
                    )
                    else -> group
                }
                // NOTE: [createTranslationsStates] is suspending, so we're deliberately
                // calling [update] outside of it.
                // It's also possible to coroutines would compete here, but in that case
                // they both would just set the same translation state in the end, so
                // it's not a problem.
                _state.update { it.replaceByID(updatedGroup) }
            }
        }
    }
}

private const val TAG = "SearchResultsViewModel"

private fun SearchResultsState.toLogsString() = buildString {
    append("[")
    append(
        groups.joinToString(", ") { group ->
            val type = when (group) {
                is LexicalItemDetailsGroupState.Loaded -> "Loaded"
                is LexicalItemDetailsGroupState.Loading -> "Loading"
                is LexicalItemDetailsGroupState.Error -> "Error"
            }
            "$type:${group.source}"
        }
    )
    append("]")
}
