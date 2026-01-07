package blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools

import arrow.core.Either
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.WordForm

interface FormsForExamplesProvider {
    suspend fun requestFor(
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ): Either<Err, List<WordForm>>
}
