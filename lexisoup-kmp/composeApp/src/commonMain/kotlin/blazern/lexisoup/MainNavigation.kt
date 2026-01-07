package blazern.lexisoup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import blazern.lexisoup.core.ui.theme.LexisoupTheme
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.feature.home.HomeRoute
import blazern.lexisoup.feature.search_results.SearchResultsRoute
import blazern.lexisoup.privacy_policy.PrivacyPolicyRoute
import io.ktor.http.encodeURLParameter

@Composable
fun MainNavigation(
    onNavHostReady: suspend (NavController) -> Unit = {},
) {
    LexisoupTheme {
        MainNavigationImpl(onNavHostReady)
    }
}

@Composable
private fun MainNavigationImpl(
    onNavHostReady: suspend (NavController) -> Unit = {},
) {
    val navController = rememberNavController()
    LaunchedEffect(navController) {
        onNavHostReady(navController)
    }

    NavHost(navController, startDestination = ROUTE_HOME) {
        composable(ROUTE_HOME) {
            HomeRoute(
                onSearch = { query, langFrom, langTo ->
                    navController.navigate(
                        "$ROUTE_SEARCH_RESULTS?" +
                                "$ARG_QUERY=${query.encodeURLParameter()}" +
                                "&$ARG_LANG_FROM=${langFrom.iso3}" +
                                "&$ARG_LANG_TO=${langTo.iso3}"
                    )
                },
                onPrivacyPolicyClick = {
                    navController.navigate(ROUTE_PRIVACY_POLICY)
                },
            )
        }

        composable(
            route = "$ROUTE_SEARCH_RESULTS?" +
                    "$ARG_QUERY={$ARG_QUERY}" +
                    "&$ARG_LANG_FROM={$ARG_LANG_FROM}" +
                    "&$ARG_LANG_TO={$ARG_LANG_TO}",
            arguments = listOf(
                navArgument(ARG_QUERY) { type = NavType.StringType },
                navArgument(ARG_LANG_FROM) { type = NavType.StringType },
                navArgument(ARG_LANG_TO) { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.read { getStringOrNull(ARG_QUERY) }.orEmpty()
            val langFrom = backStackEntry.arguments?.read { getStringOrNull(ARG_LANG_FROM) }.orEmpty()
            val langTo = backStackEntry.arguments?.read { getStringOrNull(ARG_LANG_TO) }.orEmpty()

            SearchResultsRoute(
                query,
                Lang.fromIso3(langFrom) ?: Lang.EN,
                Lang.fromIso3(langTo) ?: Lang.EN,
            )
        }

        composable(ROUTE_PRIVACY_POLICY) { PrivacyPolicyRoute() }
    }
}

private const val ROUTE_HOME = "home"
private const val ROUTE_SEARCH_RESULTS = "search_results"
private const val ROUTE_PRIVACY_POLICY = "privacy_policy"

private const val ARG_QUERY = "query"
private const val ARG_LANG_FROM = "lang_from"
private const val ARG_LANG_TO = "lang_to"
