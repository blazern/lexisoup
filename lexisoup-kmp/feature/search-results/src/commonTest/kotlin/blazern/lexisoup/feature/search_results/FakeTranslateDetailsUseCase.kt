package blazern.lexisoup.feature.search_results

import arrow.core.Either
import blazern.lexisoup.data.translator.api.Translator
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.feature.search_results.usecases.TranslateDetailsUseCase
import kotlinx.coroutines.flow.Flow

typealias ImplFoo = (List<LexicalItemDetail>, Translator, Lang, Lang) -> Flow<Either<Err, LexicalItemDetail>>

internal class FakeTranslateDetailsUseCase(
    private val impl: ImplFoo,
) : TranslateDetailsUseCase {
    override fun invoke(
        details: List<LexicalItemDetail>,
        translator: Translator,
        langFrom: Lang,
        langTo: Lang,
    ) = impl(details, translator, langFrom, langTo)
}
