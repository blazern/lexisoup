package blazern.lexisoup.data.kaikki

import arrow.core.Either
import blazern.lexisoup.core.ktor.KtorClientHolder
import blazern.lexisoup.data.kaikki.model.Entry
import blazern.lexisoup.domain.backend_address.BackendAddressProvider
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.utils.KotlinPlatform
import blazern.lexisoup.utils.getKotlinPlatform
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

internal class KaikkiClientImpl(
    private val ktorClientHolder: KtorClientHolder,
    private val backendAddressProvider: BackendAddressProvider,
    private val cpuDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : KaikkiClient {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun search(
        query: String,
        langFrom: Lang,
    ): Either<Err, List<Entry>> {
        return withContext(cpuDispatcher) {
            try {
                var result = ktorClientHolder.client.get(getUrl(query, langFrom))
                if (result.status == HttpStatusCode.NotFound) {
                    val updatedQuery = if (query.firstOrNull()?.isUpperCase() == true) {
                        query.replaceFirstChar { it.lowercase() }
                    } else {
                        query.replaceFirstChar { it.uppercase() }
                    }
                    result = ktorClientHolder.client.get(getUrl(updatedQuery, langFrom))
                }
                if (result.status == HttpStatusCode.NotFound) {
                    return@withContext Either.Right(emptyList())
                }
                val entries = result
                    .bodyAsText()
                    .lineSequence()
                    .filter { it.isNotBlank() }
                    .map { json.decodeFromString<Entry>(it) }
                    .toList()
                Either.Right(entries)
            } catch (e: Exception) {
                Either.Left(Err.from(e))
            }
        }
    }

    private suspend fun getUrl(
        query: String,
        langFrom: Lang,
    ): String {
        return when (getKotlinPlatform()) {
            KotlinPlatform.JS -> {
                val baseUrl = backendAddressProvider.baseUrl.first()
                "$baseUrl/kaikki?lang_iso3=${langFrom.iso3}&query=${query}"
            }
            else -> "https://kaikki.org/" + subwiktionaryOf(langFrom) + "/meaning/" + wordPagePostfix(query)
        }
    }
}

private fun subwiktionaryOf(lang: Lang): String {
    return when (lang) {
        Lang.RU -> "ruwiktionary/Русский"
        Lang.EN -> "dictionary/English"
        Lang.DE -> "dewiktionary/Deutsch"
        Lang.FR -> "frwiktionary/Français"
    }
}

private fun wordPagePostfix(word: String): String {
    return "${word[0]}/${word.substring(0, 2)}/${word}.jsonl"
}
