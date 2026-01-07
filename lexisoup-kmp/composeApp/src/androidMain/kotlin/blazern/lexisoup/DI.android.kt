package blazern.lexisoup

import android.content.Context
import org.koin.dsl.module

internal actual fun platformModule() = module {
    single<Context> {
        App.instance
    }
}
