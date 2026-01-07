package blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.Lang

internal class FormsAccentsEnhancerProviderImpl(
    private val formsProvider: FormsForExamplesProvider,
) : FormsAccentsEnhancerProvider {
    override suspend fun provideFor(
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ): Either<Err, FormsAccentsEnhancer> {
        return formsProvider.requestFor(query, langFrom, langTo).fold(
            { Left(it) },
            { Right(FormsAccentsEnhancerImpl(it)) }
        )
    }
}
