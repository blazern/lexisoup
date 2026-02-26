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
    ): Boolean {
        val translatableSet = detail.translatableSet
        return translatableSet != null
                && translatableSet.original.lang == langFrom
                && translator.canTranslate(translatableSet.original, langFrom, langTo)
    }
}
