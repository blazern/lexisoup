package blazern.lexisoup.data.lexisoup.graphql

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.core.getOrElse
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_BASIC
import blazern.lexisoup.graphql.model.fragment.SentenceFields
import blazern.lexisoup.graphql.model.fragment.TranslationsSetFields
import com.apollographql.apollo.api.ApolloResponse
import kotlinx.io.IOException

val ApolloResponse<*>.err: Err?
    get() {
        exception?.let {
            return Err.from(it)
        }
        if (!errors.isNullOrEmpty()) {
            val msg = errors!!.joinToString("; ") { it.message }
            return Err.from(IOException(msg))
        }
        return null
    }

fun TranslationsSetFields.toDomain(): Either<IllegalArgumentException, TranslationsSet> {
    return Right(TranslationsSet(
        original = original.toDomain()
            .getOrElse { return Left(it) },
        translations = translations.map {
            it.toDomain().getOrElse { return Left(it) }
        },
        translationsQualities = translationsQualities ?: translations.map { QUALITY_BASIC }
    ))
}

fun TranslationsSetFields.Original.toDomain():
        Either<IllegalArgumentException, Sentence> = this.sentenceFields.toDomain()

fun TranslationsSetFields.Translation.toDomain():
        Either<IllegalArgumentException, Sentence> = this.sentenceFields.toDomain()

fun SentenceFields.toDomain(): Either<IllegalArgumentException, Sentence> {
    val lang = Lang.fromIso3(langIso3)
        ?: return Left(IllegalArgumentException("Lang $langIso3 not supported"))
    return Right(Sentence(
        text = text,
        lang = lang,
        source = mapSource(source),
    ))
}

fun mapSource(remoteSource: String) = when (remoteSource) {
    "tatoeba" -> DataSource.TATOEBA
    "chatgpt" -> DataSource.CHATGPT
    "kaikki" -> DataSource.KAIKKI
    "panlex" -> DataSource.PANLEX
    "wortschatz_leipzig" -> DataSource.WORTSCHATZ_LEIPZIG
    else -> DataSource.UNKNOWN
}
