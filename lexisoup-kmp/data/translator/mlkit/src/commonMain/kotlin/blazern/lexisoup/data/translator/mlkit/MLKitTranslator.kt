package blazern.lexisoup.data.translator.mlkit

import blazern.lexisoup.data.translator.api.Translator

expect fun platformSupportsMlKitTranslator(): Boolean
expect fun createMlKitTranslator(): Translator
