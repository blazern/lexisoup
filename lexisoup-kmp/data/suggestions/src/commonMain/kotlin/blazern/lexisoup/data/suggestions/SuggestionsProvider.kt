package blazern.lexisoup.data.suggestions

import arrow.core.Either
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.Suggestion

interface SuggestionsProvider {
    suspend fun suggestionsFor(
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ): Either<Err, List<Suggestion>>
}
