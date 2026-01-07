package blazern.lexisoup

import android.app.Application

class App : Application() {
    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        initKoin()
    }

    companion object {
        internal lateinit var instance: App
    }
}
