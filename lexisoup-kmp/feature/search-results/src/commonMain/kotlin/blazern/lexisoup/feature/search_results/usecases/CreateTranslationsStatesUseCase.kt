package blazern.lexisoup.feature.search_results.usecases

import blazern.lexisoup.data.translator.aggregation.TranslatorsAggregator
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.feature.search_results.model.TranslationState

internal interface CreateTranslationsStatesUseCase {
    suspend operator fun invoke(
        details: List<LexicalItemDetail>,
        langFrom: Lang,
        langTo: Lang,
    ): List<TranslationState>
}

internal open class CreateTranslationsStatesUseCaseImpl(
    private val translators: TranslatorsAggregator,
    private val canTranslate: CanTranslateUseCase,
) : CreateTranslationsStatesUseCase {

    override suspend operator fun invoke(
        details: List<LexicalItemDetail>,
        langFrom: Lang,
        langTo: Lang,
    ): List<TranslationState> {
        val translationsStates = mutableListOf<TranslationState>()
        translatorsLoop@ for (translationSource in translators.dataSources) {
            val translator = translators.getTranslator(translationSource)
            details.forEach { detail ->
                if (canTranslate(detail, translator, langFrom, langTo, ifLangsDownloaded = false)) {
                    translationsStates.add(TranslationState.CanStart(translationSource))
                    continue@translatorsLoop
                }
                if (canTranslate(detail, translator, langFrom, langTo, ifLangsDownloaded = true)) {
                    translationsStates.add(TranslationState.MustDownloadLangs(translationSource))
                    continue@translatorsLoop
                }
            }
        }
        return translationsStates
    }
}
