package blazern.lexisoup.core.logging

internal actual fun logPlatformImpl(
    level: Logger.Level,
    tag: String,
    throwable: Throwable?,
    msg: () -> String
) {
    println("$level $tag: ${msg()}, $throwable")
}
