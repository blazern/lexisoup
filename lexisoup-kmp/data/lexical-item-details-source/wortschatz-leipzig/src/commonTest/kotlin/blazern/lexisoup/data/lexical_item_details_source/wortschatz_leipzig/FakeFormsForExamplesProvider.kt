package blazern.lexisoup.data.lexical_item_details_source.wortschatz_leipzig

import arrow.core.Either
import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.FormsForExamplesProvider
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.WordForm

internal class FakeFormsForExamplesProvider : FormsForExamplesProvider {
    data class Call(
        val query: String,
        val langFrom: Lang,
        val langTo: Lang,
    )

    val calls: MutableList<Call> = mutableListOf()

    var nextResult: Either<Err, List<WordForm>> =
        Either.Left(Err.from(IllegalStateException("nextResult not configured")))

    override suspend fun requestFor(
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ): Either<Err, List<WordForm>> {
        calls += Call(query, langFrom, langTo)
        return nextResult
    }
}
