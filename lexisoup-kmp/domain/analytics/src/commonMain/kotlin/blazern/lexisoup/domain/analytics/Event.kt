package blazern.lexisoup.domain.analytics

import blazern.lexisoup.domain.model.Lang

sealed class Event(
    val id: String,
    internal val ints: Map<String, Int> = emptyMap(),
    internal val doubles: Map<String, Double> = emptyMap(),
    internal val strings: Map<String, String> = emptyMap(),
) {
    init {
        require(!intersect(ints.keys, doubles.keys, strings.keys))
    }

    data class ScreenOpened(val name: String) : Event(
        "screen_view",
        strings = mapOf("screen_name" to name),
    )
    data class Search(
        val query: String,
        val langFrom: Lang,
        val langTo: Lang,
    ) : Event(
        "search",
        strings = mapOf(
            "query" to query,
            "lang_from" to langFrom.iso3,
            "lang_to" to langTo.iso3,
        ),
    )
}

private fun <T> intersect(vararg sets: Set<T>): Boolean {
    val seen = mutableSetOf<T>()
    for (set in sets) {
        for (item in set) {
            if (!seen.add(item)) {
                return true
            }
        }
    }
    return false
}
