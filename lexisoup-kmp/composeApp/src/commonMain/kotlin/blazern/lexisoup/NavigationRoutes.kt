package blazern.lexisoup

import kotlinx.serialization.Serializable

interface NavigationRoute

@Serializable
data object Home : NavigationRoute

@Serializable
data class SearchResults(
    val query: String,
//    val langFrom: Lang,
//    val langTo: Lang,
) : NavigationRoute
