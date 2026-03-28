package blazern.lexisoup.data.suggestions

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.core.getOrElse
import blazern.lexisoup.core.logging.Log
import blazern.lexisoup.data.lexisoup.graphql.LexisoupApolloClientHolder
import blazern.lexisoup.data.lexisoup.graphql.err
import blazern.lexisoup.data.lexisoup.graphql.toDomain
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.Suggestion
import blazern.lexisoup.graphql.model.SuggestionsQuery
import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

internal class SuggestionsProviderImpl(
    private val apolloClientHolder: LexisoupApolloClientHolder,
) : SuggestionsProvider {
    private val apollo: Flow<ApolloClient>
        get() = apolloClientHolder.client

    override suspend fun suggestionsFor(
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ): Either<Err, List<Suggestion>> {
        val apolloResult = apollo.first().query(
            SuggestionsQuery(
                query,
                langFromIso3 = langFrom.iso3,
                langToIso3 = langTo.iso3,
            )
        ).execute()

        apolloResult.err?.let { return Left(it) }

        val data = apolloResult.data ?: return Right(emptyList())

        val result = data.suggestions.map {
            Suggestion(
                text = it.text,
                lang = langFrom,
                translations = it.translations.mapNotNull {
                    it.sentenceFields.toDomain().getOrElse { e ->
                        Log.e(TAG, e) { "Could not convert translations" }
                        null
                    }
                }
            )
        }
        return Right(result)
    }

    private companion object {
        const val TAG = "SuggestionsProviderImpl"
    }
}
