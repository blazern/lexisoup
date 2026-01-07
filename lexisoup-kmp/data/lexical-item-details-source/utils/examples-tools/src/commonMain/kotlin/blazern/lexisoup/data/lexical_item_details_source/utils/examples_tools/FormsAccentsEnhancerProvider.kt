package blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools

import arrow.core.Either
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.Lang

interface FormsAccentsEnhancerProvider {
    suspend fun provideFor(
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ): Either<Err, FormsAccentsEnhancer>
}

