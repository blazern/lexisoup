package blazern.lexisoup.feature.search_results

import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.feature.search_results.model.TranslationState
import blazern.lexisoup.feature.search_results.usecases.CreateTranslationsStatesUseCase

internal class FakeCreateTranslationsStatesUseCase(
    private val impl: (List<LexicalItemDetail>, Lang, Lang) -> List<TranslationState>,
) : CreateTranslationsStatesUseCase {
    override suspend fun invoke(
        details: List<LexicalItemDetail>,
        langFrom: Lang,
        langTo: Lang
    ) = impl(details, langFrom, langTo)
}
