package blazern.lexisoup.feature.search_results.usecases

import blazern.lexisoup.data.translator.api.Translator
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.feature.search_results.model.translatableSet

internal open class CanTranslateUseCase {
    suspend operator fun invoke(
        detail: LexicalItemDetail,
        translator: Translator,
        langFrom: Lang,
        langTo: Lang,
        ifLangsDownloaded: Boolean,
    ): Boolean {
        val translatableSet = detail.translatableSet
        if (translatableSet?.original?.lang != langFrom) {
            return false
        }
        return translator.canTranslate(
            translatableSet.original,
            langFrom,
            langTo,
            ifLangsDownloaded,
        )
    }
}
