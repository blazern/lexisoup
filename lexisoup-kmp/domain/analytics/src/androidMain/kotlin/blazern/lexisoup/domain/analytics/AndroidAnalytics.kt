package blazern.lexisoup.domain.analytics

import android.os.Bundle
import blazern.lexisoup.core.logging.Log
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics

class AndroidAnalytics : Analytics {
    private val firebaseAnalytics = Firebase.analytics

    override fun log(event: Event) {
        Log.i(TAG) { "event: $event" }
        firebaseAnalytics.logEvent(event.id, event.bundle())
    }

    private companion object {
        const val TAG = "AndroidAnalytics"
    }
}

internal fun Event.bundle() = Bundle().apply {
    ints.forEach { (key, value) -> putInt(key, value) }
    doubles.forEach { (key, value) -> putDouble(key, value) }
    strings.forEach { (key, value) -> putString(key, value) }
}
