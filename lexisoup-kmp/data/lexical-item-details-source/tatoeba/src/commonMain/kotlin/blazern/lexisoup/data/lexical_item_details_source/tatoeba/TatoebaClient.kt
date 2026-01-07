package blazern.lexisoup.data.lexical_item_details_source.tatoeba

import arrow.core.Either
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.TranslationsSet

internal interface TatoebaClient {
    suspend fun search(
        query: String,
        langFrom: Lang,
        langTo: Lang,
        page: Int,
    ): Either<Err, List<TranslationsSet>>
}
