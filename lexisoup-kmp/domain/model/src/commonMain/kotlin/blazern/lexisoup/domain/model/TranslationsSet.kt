package blazern.lexisoup.domain.model

import blazern.lexisoup.core.logging.Log

@ConsistentCopyVisibility
data class TranslationsSet private constructor(
    val original: Sentence,
    /**
     * Will always get sorted by [translationsQualities] on object instantiation.
     */
    val translations: List<Sentence>,
    /**
     * Values from 0 to 9, marking the expected quality of the translations.
     */
    val translationsQualities: List<Int>,
) {
    companion object {
        operator fun invoke(
            original: Sentence,
            translations: List<Sentence>,
            translationsQualities: List<Int>
        ): TranslationsSet {
            if (translationsQualities.size != translations.size) {
                Log.e(TAG) { "Sizes do not match: $translationsQualities $translations" }
            }

            val qualities = when {
                translationsQualities.size > translations.size ->
                    translationsQualities.take(translations.size)
                translationsQualities.size < translations.size ->
                    translationsQualities + List(translations.size - translationsQualities.size) { QUALITY_BASIC }
                else -> translationsQualities
            }.map { it.coerceIn(QUALITY_NONE, QUALITY_MAX) }

            val (sortedTranslations, sortedQualities) = translations
                .zip(qualities)
                .sortedByDescending { it.second }
                .unzip()

            return TranslationsSet(original, sortedTranslations, sortedQualities)
        }

        const val QUALITY_NONE = 0
        const val QUALITY_BASIC = 1
        const val QUALITY_MAX = 9
        private const val TAG = "TranslationsSet"
    }
}

fun TranslationsSet.copy(
    original: Sentence = this.original,
    translations: List<Sentence> = this.translations,
    translationsQualities: List<Int> = this.translationsQualities,
): TranslationsSet = TranslationsSet(original, translations, translationsQualities)
