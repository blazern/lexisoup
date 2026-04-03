package blazern.lexisoup.core.logging

interface Logger {
    fun log(level: Level, tag: String, throwable: Throwable? = null, msg: ()->String)

    enum class Level {
        DEBUG,
        ERROR,
        INFO,
        VERBOSE,
        WARNING,
        ;

        override fun toString() = when (this) {
            DEBUG -> "d"
            ERROR -> "e"
            INFO -> "i"
            VERBOSE -> "v"
            WARNING -> "w"
        }
    }
}
