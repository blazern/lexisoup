package blazern.lexisoup.data.translator.backend

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import blazern.lexisoup.data.lexisoup.graphql.LexisoupApolloClientHolder
import blazern.lexisoup.data.lexisoup.graphql.err
import blazern.lexisoup.data.lexisoup.graphql.mapSource
import blazern.lexisoup.data.translator.api.Translator
import blazern.lexisoup.domain.config.api.ConfigProvider
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_BASIC
import blazern.lexisoup.graphql.model.TranslateQuery
import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class BackendTranslator(
    configProvider: ConfigProvider,
    private val apolloClientHolder: LexisoupApolloClientHolder,
) : Translator {
    private val apollo: Flow<ApolloClient>
        get() = apolloClientHolder.client

    override val source = DataSource.Backend(impl = null)
    override val capabilities: Flow<Translator.Capabilities> = configProvider.config.map {
        Translator.Capabilities(
            availableLangs = Lang.entries.associateWith { Lang.entries.toSet() },
            downloadableLangs = emptyMap(),
            availableOffline = false,
            textLengthMin = it.translateTextLengthMin,
            textLengthMax = it.translateTextLengthMax,
            translateBatchSizeLimit = it.translateBatchSizeLimit,
        )
    }

    override fun translate(
        sentences: List<Sentence>,
        langFrom: Lang,
        langTo: Lang
    ): Flow<Either<Err, TranslationsSet>> = flow {
        val result = apollo.first().query(
            TranslateQuery(
                texts = sentences.map { it.text },
                langFromIso3 = langFrom.iso3,
                langToIso3 = langTo.iso3,
            )
        ).execute()

        result.err?.let {
            emit(Left(it))
            return@flow
        }

        val data = result.data
        if (data == null) {
            emit(Left(Err.Net.InvalidResponse("No data returned")))
            return@flow
        }

        data.translate.translations.forEachIndexed { index, text ->
            emit(Right(TranslationsSet(
                original = sentences[index],
                translations = listOf(Sentence(
                    text = text,
                    lang = langTo,
                    source = mapSource(data.translate.source)
                )),
                translationsQualities = listOf(QUALITY_BASIC),
            )))
        }
    }

    override suspend fun downloadLangsPair(
        lang1: Lang,
        lang2: Lang
    ) = error("Backend translator is not available offline")
}
