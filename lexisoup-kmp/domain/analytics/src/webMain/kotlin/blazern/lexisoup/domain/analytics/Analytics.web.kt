package blazern.lexisoup.domain.analytics

import blazern.lexisoup.core.logging.Log

internal actual fun createAnalytics() = object : Analytics {
    override fun log(event: Event) {
        Log.i(TAG) { "event: $event" }
    }
}

const val TAG = "Analytics.web"
