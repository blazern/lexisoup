package blazern.lexisoup.data.kaikki

import arrow.core.Either
import blazern.lexisoup.data.kaikki.model.Entry
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.Lang

interface KaikkiClient {
    suspend fun search(
        query: String,
        langFrom: Lang,
    ): Either<Err, List<Entry>>
}
