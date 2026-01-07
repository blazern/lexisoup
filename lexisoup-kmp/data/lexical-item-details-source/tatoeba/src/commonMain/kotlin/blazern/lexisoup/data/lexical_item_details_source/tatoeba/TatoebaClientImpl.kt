package blazern.lexisoup.data.lexical_item_details_source.tatoeba

import arrow.core.Either
import blazern.lexisoup.core.ktor.KtorClientHolder
import blazern.lexisoup.data.lexical_item_details_source.tatoeba.api.ApiResponse
import blazern.lexisoup.domain.backend_address.BackendAddressProvider
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.utils.KotlinPlatform
import blazern.lexisoup.utils.getKotlinPlatform
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.flow.first

internal class TatoebaClientImpl(
    private val ktorClientHolder: KtorClientHolder,
    private val backendAddressProvider: BackendAddressProvider,
) : TatoebaClient {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun search(
        query: String,
        langFrom: Lang,
        langTo: Lang,
        page: Int,
    ): Either<Err, List<TranslationsSet>> {
        val url = getUrlBase()
        val response: ApiResponse = try {
            ktorClientHolder.client.get(url) {
                parameter("from", langFrom.iso3)
                parameter("to", langTo.iso3)
                parameter("trans_to", langTo.iso3)
                parameter("query", query)
                parameter("trans_filter", "limit")
                parameter("trans_link", "direct")
                parameter("page", page)
            }.body()
        } catch (e: Exception) {
            return Either.Left(Err.from(e))
        }

        val result = mutableListOf<TranslationsSet>()
        for (sentenceTatoeba in response.results) {
            val translations = mutableListOf<Sentence>()
            for (translationTatoeba in sentenceTatoeba.translations.flatten()) {
                if (translationTatoeba.lang == langTo.iso3) {
                    translations.add(Sentence(
                        text = translationTatoeba.text,
                        lang = langTo,
                        source = DataSource.TATOEBA,
                    ))
                }
            }
            if (translations.isNotEmpty()) {
                result.add(TranslationsSet(
                    original = Sentence(
                        text = sentenceTatoeba.text,
                        lang = langFrom,
                        source = DataSource.TATOEBA,
                    ),
                    translations = translations,
                    translationsQualities = translations.map { TranslationsSet.QUALITY_MAX }
                ))
            }
        }
        return Either.Right(result)
    }

    private suspend fun getUrlBase(): String {
        return when (getKotlinPlatform()) {
            KotlinPlatform.JS -> {
                val baseUrl = backendAddressProvider.baseUrl.first()
                "$baseUrl/tatoeba"
            }
            else -> "https://tatoeba.org/en/api_v0/search"
        }
    }
}
