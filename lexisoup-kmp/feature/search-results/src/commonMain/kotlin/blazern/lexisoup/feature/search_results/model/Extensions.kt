package blazern.lexisoup.feature.search_results.model

import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.TranslationsSet

internal val LexicalItemDetail.translatableSet: TranslationsSet?
    get() {
        val translationsSet = when (this) {
            is LexicalItemDetail.Explanation -> translationsSet
            is LexicalItemDetail.Example -> translationsSet
            is LexicalItemDetail.Etymology -> translationsSet
            else -> return null
        }
        if (translationsSet.translations.isNotEmpty()) {
            // Already translated
            return null
        }
        return translationsSet
    }
