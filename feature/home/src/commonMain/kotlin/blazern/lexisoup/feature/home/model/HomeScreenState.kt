package blazern.lexisoup.feature.home.model

import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.Suggestion

internal data class HomeScreenState(
    val langFrom: Lang?,
    val langTo: Lang?,
    val query: String,
    val canSearch: Boolean,
    val suggestions: List<Suggestion>,
    val suggestionsTarget: String,
    val isLocalhost: Boolean?,
)
