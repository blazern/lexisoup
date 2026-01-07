@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package blazern.lexisoup.core.logging

expect object Log {
    fun d(tag: String, throwable: Throwable? = null, msg: ()->String)
    fun e(tag: String, throwable: Throwable? = null, msg: ()->String)
    fun i(tag: String, throwable: Throwable? = null, msg: ()->String)
    fun v(tag: String, throwable: Throwable? = null, msg: ()->String)
    fun w(tag: String, throwable: Throwable? = null, msg: ()->String)
}
