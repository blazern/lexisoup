package blazern.lexisoup.feature.search_results.model

import blazern.lexisoup.domain.model.Lang

internal data class SearchRequest(
    val query: String,
    val langFrom: Lang,
    val langTo: Lang,
)
