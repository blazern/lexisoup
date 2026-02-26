package blazern.lexisoup.feature.search_results.ui.list

import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.feature.search_results.model.LexicalItemDetailsGroupState
import blazern.lexisoup.feature.search_results.model.SearchRequest

internal interface LexicalItemDetailCallbacks {
    fun onTextCopy(text: String)
    fun onLoadingDetailVisible(loading: LexicalItemDetailsGroupState.Loading)
    fun onFixErrorRequest(error: LexicalItemDetailsGroupState.Error)
    fun onNewSearch(searchRequest: SearchRequest)
    fun onTranslateRequest(detailsGroup: LexicalItemDetailsGroupState.Loaded, translator: DataSource)

    object Stub : LexicalItemDetailCallbacks {
        override fun onTextCopy(text: String) = Unit
        override fun onLoadingDetailVisible(loading: LexicalItemDetailsGroupState.Loading) = Unit
        override fun onFixErrorRequest(error: LexicalItemDetailsGroupState.Error) = Unit
        override fun onNewSearch(searchRequest: SearchRequest) = Unit
        override fun onTranslateRequest(
            detailsGroup: LexicalItemDetailsGroupState.Loaded,
            translator: DataSource,
        ) = Unit
    }
}
