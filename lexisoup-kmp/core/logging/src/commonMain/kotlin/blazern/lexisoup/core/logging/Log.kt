package blazern.lexisoup.core.logging

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

object Log {
    @Volatile
    private var loggers: List<Logger> = listOf(
        PlatformLogger(),
    )

    /**
     * NOTE: the logger will not be added immediately if the function
     * is not called on the main thread.
     */
    fun addLogger(logger: Logger) {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(Dispatchers.Main.immediate) {
            loggers += logger
        }
    }

    fun d(tag: String, throwable: Throwable? = null, msg: ()->String) =
        log(Logger.Level.DEBUG, tag, throwable, msg)

    fun e(tag: String, throwable: Throwable? = null, msg: ()->String) =
        log(Logger.Level.ERROR, tag, throwable, msg)

    fun i(tag: String, throwable: Throwable? = null, msg: ()->String) =
        log(Logger.Level.INFO, tag, throwable, msg)

    fun v(tag: String, throwable: Throwable? = null, msg: ()->String) =
        log(Logger.Level.VERBOSE, tag, throwable, msg)

    fun w(tag: String, throwable: Throwable? = null, msg: ()->String) =
        log(Logger.Level.WARNING, tag, throwable, msg)

    @Suppress("MemberNameEqualsClassName")
    private fun log(
        level: Logger.Level,
        tag: String,
        throwable: Throwable?,
        msg: ()->String,
    ) {
        loggers.forEach { it.log(level, tag, throwable, msg) }
    }
}
