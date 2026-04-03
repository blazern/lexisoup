package blazern.lexisoup.core.logging

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics

private val inJunitTest: Boolean by lazy {
    try {
        Class.forName("org.junit.runner.JUnitCore")
        true
    } catch (_: ClassNotFoundException) {
        false
    }
}

internal actual fun logPlatformImpl(
    level: Logger.Level,
    tag: String,
    throwable: Throwable?,
    msg: () -> String
) {
    val msgStr = msg()
    val msgFull = "$level $tag: $msgStr" + throwable?.let { ", $it" }.orEmpty()
    if (inJunitTest) {
        println(msgFull)
        return
    }
    when (level) {
        Logger.Level.ERROR -> {
            Firebase.crashlytics.log(msgFull)
            android.util.Log.e(tag, msgStr, throwable)
        }
        Logger.Level.WARNING -> {
            Firebase.crashlytics.log(msgFull)
            android.util.Log.w(tag, msgStr, throwable)
        }
        Logger.Level.INFO -> {
            Firebase.crashlytics.log(msgFull)
            android.util.Log.i(tag, msgStr, throwable)
        }
        Logger.Level.DEBUG -> android.util.Log.d(tag, msgStr, throwable)
        Logger.Level.VERBOSE -> android.util.Log.v(tag, msgStr, throwable)
    }
}
