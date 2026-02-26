package blazern.lexisoup.feature.search_results.ui.list.model

import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.feature.search_results.model.LexicalItemDetailsGroupState

internal sealed interface ExampleState {
    data class Loaded(
        val example: LexicalItemDetail.Example,
        val parentGroup: LexicalItemDetailsGroupState.Loaded,
    ) : ExampleState
    data class Loading(val loading: LexicalItemDetailsGroupState.Loading) : ExampleState
    data class Error(val error: LexicalItemDetailsGroupState.Error) : ExampleState
}