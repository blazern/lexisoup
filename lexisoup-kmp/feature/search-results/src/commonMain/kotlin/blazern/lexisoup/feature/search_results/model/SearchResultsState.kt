package blazern.lexisoup.feature.search_results.model

import androidx.compose.runtime.Immutable
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.LexicalItemDetail
import kotlin.collections.plus
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Immutable
internal data class SearchResultsState(
    val groups: List<LexicalItemDetailsGroupState> = emptyList(),
)

internal fun SearchResultsState.replaceAllButLoadedWith(
    item: Item,
    source: DataSource,
    types: Set<LexicalItemDetail.Type>,
): SearchResultsState {
    val predicate = { state: LexicalItemDetailsGroupState ->
        state.source == source && state !is LexicalItemDetailsGroupState.Loaded
    }
    val id = groups.firstOrNull(predicate)?.id ?: randomId()
    var result = this.removeAllButLoadedFor(source)
    return when (item) {
        is Item.Page -> result.add(id, item, source)
        is Item.Failure -> result.addFailure(id, item, source, types)
    }
}

internal fun SearchResultsState.removeAllButLoadedFor(
    source: DataSource,
): SearchResultsState {
    return copy(
        groups = groups.filter {
            it.source != source || it is LexicalItemDetailsGroupState.Loaded
        }
    )
}

internal fun SearchResultsState.removeErrorsFor(
    source: DataSource,
): SearchResultsState {
    return copy(
        groups = groups.filter {
            it.source != source || it !is LexicalItemDetailsGroupState.Error
        }
    )
}

internal fun SearchResultsState.add(
    id: String,
    page: Item.Page,
    source: DataSource,
): SearchResultsState {
    return add(
        LexicalItemDetailsGroupState.Loaded(
            id = id,
            details = page.details,
            types = page.details.map { it.type }.toSet(),
            source = source,
        )
    )
}

private fun SearchResultsState.add(
    state: LexicalItemDetailsGroupState,
): SearchResultsState {
    return copy(
        groups = (groups + listOf(state)).sortedBy { it.source.priority }
    )
}

internal fun SearchResultsState.addFailure(
    id: String,
    failure: LexicalItemDetailsSource.Item.Failure,
    source: DataSource,
    types: Set<LexicalItemDetail.Type>,
): SearchResultsState {
    return add(
        LexicalItemDetailsGroupState.Error(
            id = id,
            err = failure.err,
            types = types,
            source = source,
        )
    )
}

internal fun SearchResultsState.addLoadingFor(
    source: DataSource,
    types: Set<LexicalItemDetail.Type>,
): SearchResultsState {
    return add(LexicalItemDetailsGroupState.Loading(randomId(), types, source))
}

internal val DataSource.priority: Int
    @Suppress("MagicNumber")
    get() {
        return when (this) {
            DataSource.PANLEX -> 0
            DataSource.TATOEBA -> 1
            DataSource.CHATGPT -> 2
            DataSource.KAIKKI -> 3
            DataSource.WORTSCHATZ_LEIPZIG -> 4
            DataSource.UNKNOWN -> 100
        }
    }

@OptIn(ExperimentalUuidApi::class)
private fun randomId() = Uuid.random().toString()
