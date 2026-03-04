package blazern.lexisoup.data.lexical_item_details_source.panlex

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.core.getOrElse
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.data.lexical_item_details_source.utils.cache.LexicalItemDetailsSourceCacher
import blazern.lexisoup.data.lexisoup.graphql.LexisoupApolloClientHolder
import blazern.lexisoup.data.lexisoup.graphql.err
import blazern.lexisoup.data.lexisoup.graphql.mapSource
import blazern.lexisoup.data.lexisoup.graphql.toDomain
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_MAX
import blazern.lexisoup.domain.model.copy
import blazern.lexisoup.graphql.model.LexicalItemsFromPanLexQuery
import blazern.lexisoup.utils.onlyLetters
import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class PanLexLexicalItemDetailsSource(
    private val apolloClientHolder: LexisoupApolloClientHolder,
    private val cacher: LexicalItemDetailsSourceCacher,
) : LexicalItemDetailsSource {
    private val apollo: Flow<ApolloClient>
        get() = apolloClientHolder.client

    override val source = DataSource.PanLex
    override val types = setOf(
        LexicalItemDetail.Type.WORD_TRANSLATIONS,
        LexicalItemDetail.Type.SYNONYMS,
    )

    override fun request(
        query: String,
        langFrom: Lang,
        langTo: Lang
    ): Flow<Item> = cacher.retrieveOrExecute(source, query, langFrom, langTo) {
        requestImpl(query, langFrom, langTo)
    }

    private fun requestImpl(
        query: String,
        langFrom: Lang,
        langTo: Lang
    ): Flow<Item> = flow {
        while (true) {
            val result = apolloRequest(query, langFrom, langTo)
            result.fold(
                { emit(Item.Failure(it)) },
                {
                    emit(Item.Page(
                        details = it,
                        nextPageTypes = types,
                    ))
                    return@flow
                }
            )
        }
    }

    private suspend fun apolloRequest(
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ): Either<Err, List<LexicalItemDetail>> {
        val result = apollo.first().query(
            LexicalItemsFromPanLexQuery(
                query,
                langFromIso3 = langFrom.iso3,
                langToIso3 = langTo.iso3,
            )
        ).execute()

        result.err?.let { return Left(it) }

        val data = result.data ?: return Right(emptyList())

        val items = data.panlex.mapNotNull {
            it.toDomain().getOrElse { return Left(Err.from(it)) }
        }
        return Right(items)
    }
}

private fun LexicalItemsFromPanLexQuery.Panlex.toDomain(): Either<IllegalArgumentException, LexicalItemDetail?> {
    onWordTranslations?.let {
        val ts = it.translationsSet.translationsSetFields
        val translationsSet = ts
            .toDomain()
            .map { it.reorganize() }
            .getOrElse { return Left(it) }
        return Right(LexicalItemDetail.WordTranslations(
            translationsSet = translationsSet,
            source = mapSource(it.source)
        ))
    }
    onSynonyms?.let {
        val ts = it.translationsSet.translationsSetFields
        return Right(LexicalItemDetail.Synonyms(
            sentences = ts.toDomain()
                .map { it.translations }
                .getOrElse { return Left(it) },
            source = mapSource(it.source)
        ))
    }
    // New field in a newer version of the backend was added apparently
    return Right(null)
}

private fun TranslationsSet.reorganize(): TranslationsSet {
    val normalizedTranslations = translations.map {
        it.copy(text = it.text.onlyLetters())
    }

    // keeps insertion order
    val firstIndexByText = LinkedHashMap<String, Int>()
    val dedupedTranslations = mutableListOf<Sentence>()
    val dedupedQualities = mutableListOf<Int>()

    normalizedTranslations.forEachIndexed { index, translation ->
        val key = translation.text
        val quality = translationsQualities[index]

        val existingIndex = firstIndexByText[key]
        if (existingIndex == null) {
            firstIndexByText[key] = dedupedTranslations.size
            dedupedTranslations += translation
            dedupedQualities += quality
        } else {
            dedupedQualities[existingIndex] = dedupedQualities[existingIndex] + quality
        }
    }

    val sortedPairs = dedupedTranslations.zip(dedupedQualities)
        .sortedByDescending { it.second }

    return copy(
        translations = sortedPairs.map { it.first },
        translationsQualities = sortedPairs.map { minOf(it.second, QUALITY_MAX) },
    )
}
