package blazern.lexisoup.core.logging

actual object Log {
    actual fun d(tag: String, throwable: Throwable?, msg: () -> String) {
        println("d $tag: ${msg()}, $throwable")
    }

    actual fun e(tag: String, throwable: Throwable?, msg: () -> String) {
        println("e $tag: ${msg()}, $throwable")
    }

    actual fun i(tag: String, throwable: Throwable?, msg: () -> String) {
        println("i $tag: ${msg()}, $throwable")
    }

    actual fun v(tag: String, throwable: Throwable?, msg: () -> String) {
        println("v $tag: ${msg()}, $throwable")
    }

    actual fun w(tag: String, throwable: Throwable?, msg: () -> String) {
        println("w $tag: ${msg()}, $throwable")
    }
}