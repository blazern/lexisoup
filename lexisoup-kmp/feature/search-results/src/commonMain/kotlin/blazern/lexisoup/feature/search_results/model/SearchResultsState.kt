@file:Suppress("TooManyFunctions")

package blazern.lexisoup.feature.search_results.model

import androidx.compose.runtime.Immutable
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.LexicalItemDetail
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Immutable
internal data class SearchResultsState(
    val groups: List<LexicalItemDetailsGroupState> = emptyList(),
)

internal fun SearchResultsState.replaceMatchingOrJustAdd(
    item: Item.Page,
    source: DataSource,
    translationStates: List<TranslationState>,
    matches: (LexicalItemDetailsGroupState) -> Boolean,
) = replaceOrAddMatchingImpl(item, source, null, translationStates, matches)

internal fun SearchResultsState.replaceMatchingOrJustAdd(
    item: Item.Failure,
    source: DataSource,
    types: Set<LexicalItemDetail.Type>,
    matches: (LexicalItemDetailsGroupState) -> Boolean,
) = replaceOrAddMatchingImpl(item, source, types, null, matches)

private fun SearchResultsState.replaceOrAddMatchingImpl(
    item: Item,
    source: DataSource,
    types: Set<LexicalItemDetail.Type>?,
    translationStates: List<TranslationState>?,
    matches: (LexicalItemDetailsGroupState) -> Boolean,
): SearchResultsState {
    val matchedIds = groups.filter(matches).map { it.id }
    return if (matchedIds.isNotEmpty()) {
        val idToKeep = matchedIds.first()
        val restToRemove = matchedIds.drop(1).toSet()

        val replaced = when (item) {
            is Item.Page -> {
                requireNotNull(translationStates)
                replace(idToKeep, item, source, translationStates)
            }
            is Item.Failure -> {
                requireNotNull(types)
                replace(idToKeep, item, source, types)
            }
        }
        replaced.remove(restToRemove)
    } else {
        when (item) {
            is Item.Page -> {
                requireNotNull(translationStates)
                add(item, source, translationStates)
            }
            is Item.Failure -> {
                requireNotNull(types)
                addFailure(randomId(), item, source, types)
            }
        }
    }
}

internal fun SearchResultsState.replace(
    id: String,
    item: Item.Page,
    source: DataSource,
    translationStates: List<TranslationState>,
) = replaceImpl(id, item, source, null, translationStates)

internal fun SearchResultsState.replace(
    id: String,
    item: Item.Failure,
    source: DataSource,
    types: Set<LexicalItemDetail.Type>,
) = replaceImpl(id, item, source, types, null)

private fun SearchResultsState.replaceImpl(
    id: String,
    item: Item,
    source: DataSource,
    types: Set<LexicalItemDetail.Type>?,
    translationStates: List<TranslationState>?,
): SearchResultsState {
    val result = this.remove(setOf(id))
    return when (item) {
        is Item.Page -> {
            requireNotNull(translationStates)
            result.add(id, item, source, translationStates)
        }
        is Item.Failure -> {
            requireNotNull(types)
            result.addFailure(id, item, source, types)
        }
    }
}

internal fun SearchResultsState.remove(
    ids: Set<String>,
) = copy(groups = groups.filter { !ids.contains(it.id) })

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
    page: Item.Page,
    source: DataSource,
    translationStates: List<TranslationState>,
) = add(randomId(), page, source, translationStates)

private fun SearchResultsState.add(
    id: String,
    page: Item.Page,
    source: DataSource,
    translationStates: List<TranslationState>,
): SearchResultsState {
    return add(
        LexicalItemDetailsGroupState.Loaded(
            id = id,
            details = page.details,
            types = page.details.map { it.type }.toSet(),
            source = source,
            translationStates = translationStates,
        )
    )
}

private fun SearchResultsState.add(
    group: LexicalItemDetailsGroupState,
): SearchResultsState {
    return if (groups.map { it.id }.contains(group.id)) {
        replaceByID(group)
    } else {
        copy(
            groups = (groups + listOf(group)).sortedBy { it.source.priority }
        )
    }
}

internal fun SearchResultsState.addFailure(
    id: String,
    failure: Item.Failure,
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

internal fun SearchResultsState.replaceByID(
    group: LexicalItemDetailsGroupState,
): SearchResultsState {
    return copy(
        groups = groups.map { if (it.id == group.id) group else it }
    )
}

internal val DataSource.priority: Int
    @Suppress("MagicNumber")
    get() {
        return when (this) {
            DataSource.PanLex -> 0
            DataSource.Tatoeba -> 1
            DataSource.ChatGPT -> 2
            DataSource.Kaikki -> 3
            DataSource.WortschatzLeipzig -> 4
            DataSource.DeepL -> 5
            DataSource.MlKit -> 6
            is DataSource.Backend -> this.impl?.priority ?: 50
            is DataSource.Other -> 100
        }
    }

@OptIn(ExperimentalUuidApi::class)
private fun randomId() = Uuid.random().toString()
