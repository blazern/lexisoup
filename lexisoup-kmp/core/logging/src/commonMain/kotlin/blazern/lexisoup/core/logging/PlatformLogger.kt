package blazern.lexisoup.core.logging

class PlatformLogger : Logger {
    override fun log(
        level: Logger.Level,
        tag: String,
        throwable: Throwable?,
        msg: () -> String
    ) {
        logPlatformImpl(level, tag, throwable, msg)
    }
}

internal expect fun logPlatformImpl(
    level: Logger.Level,
    tag: String,
    throwable: Throwable? = null,
    msg: ()->String,
)
