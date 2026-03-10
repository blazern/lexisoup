package blazern.lexisoup.data.translator.mlkit

import blazern.lexisoup.data.translator.api.Translator

actual fun platformSupportsMlKitTranslator() = false
actual fun createMlKitTranslator(): Translator =
    throw NotImplementedError("Web does not support ML Kit")
