package blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.core.getOrElse
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.data.lexical_item_details_source.kaikki.KaikkiLexicalItemDetailsSource
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.WordForm
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class FormsForExamplesProviderImpl(
    private val kaikki: KaikkiLexicalItemDetailsSource,
) : FormsForExamplesProvider {
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun requestFor(
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ): Either<Err, List<WordForm>> {
        val flow = kaikki.request(query, langFrom, langTo).map {
            when (it) {
                is Item.Failure -> flowOf(Left(it.err))
                is Item.Page -> it.details.map { Right(it) }.asFlow()
            }
        }.flattenConcat()
        val formsRes = flow.firstOrNull {
            it.fold(
                { true },
                { it is LexicalItemDetail.Forms }
            )
        }
        val forms = formsRes?.getOrElse {
            return Left(it)
        } as LexicalItemDetail.Forms?
            ?: return Left(Err.from(IllegalStateException("No Forms returned from Kaikki")))

        val formsVal = forms.value
        if (formsVal !is LexicalItemDetail.Forms.Value.Detailed) {
            return Left(Err.from(IllegalStateException("Forms without Value: $forms")))
        }
        return Right(formsVal.forms.toFormsForExamples())
    }
}

private fun List<WordForm>.toFormsForExamples() =
    map { it.withoutPronoun().withoutArticle() }
        .distinct()
        .filter { it.wordsCount == 1 && !it.auxiliary }
