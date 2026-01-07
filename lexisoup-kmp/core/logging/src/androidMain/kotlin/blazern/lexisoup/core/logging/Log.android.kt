package blazern.lexisoup.core.logging

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object Log {
    private val inJunitTest: Boolean by lazy {
        try {
            Class.forName("org.junit.runner.JUnitCore")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    actual fun d(tag: String, throwable: Throwable?, msg: ()->String) {
        if (!inJunitTest) {
            android.util.Log.d(tag, msg(), throwable)
        } else {
            println("d $tag: ${msg()}, $throwable")
        }
    }

    actual fun e(tag: String, throwable: Throwable?, msg: ()->String) {
        if (!inJunitTest) {
            android.util.Log.e(tag, msg(), throwable)
        } else {
            println("e $tag: ${msg()}, $throwable")
        }
    }

    actual fun i(tag: String, throwable: Throwable?, msg: ()->String) {
        if (!inJunitTest) {
            android.util.Log.i(tag, msg(), throwable)
        } else {
            println("i $tag: ${msg()}, $throwable")
        }
    }

    actual fun v(tag: String, throwable: Throwable?, msg: ()->String) {
        if (!inJunitTest) {
            android.util.Log.v(tag, msg(), throwable)
        } else {
            println("v $tag: ${msg()}, $throwable")
        }
    }

    actual fun w(tag: String, throwable: Throwable?, msg: ()->String) {
        if (!inJunitTest) {
            android.util.Log.w(tag, msg(), throwable)
        } else {
            println("w $tag: ${msg()}, $throwable")
        }
    }
}
