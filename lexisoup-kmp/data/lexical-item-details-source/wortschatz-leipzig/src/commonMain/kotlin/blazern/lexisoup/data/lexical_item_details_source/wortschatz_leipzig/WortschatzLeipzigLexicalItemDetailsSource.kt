package blazern.lexisoup.data.lexical_item_details_source.wortschatz_leipzig

import blazern.lexisoup.core.ktor.KtorClientHolder
import blazern.lexisoup.core.logging.Log
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.FormsForExamplesProvider
import blazern.lexisoup.data.lexical_item_details_source.wortschatz_leipzig.model.LeipzigSentence
import blazern.lexisoup.data.lexical_item_details_source.wortschatz_leipzig.model.LeipzigSentencesResponse
import blazern.lexisoup.domain.backend_address.BackendAddressProvider
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.utils.KotlinPlatform
import blazern.lexisoup.utils.getKotlinPlatform
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.encodeURLPathPart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class WortschatzLeipzigLexicalItemDetailsSource(
    private val backendAddressProvider: BackendAddressProvider,
    private val ktorClientHolder: KtorClientHolder,
    private val formsForExamplesProvider: FormsForExamplesProvider,
) : LexicalItemDetailsSource {

    override val source = DataSource.WORTSCHATZ_LEIPZIG
    override val types = setOf(LexicalItemDetail.Type.EXAMPLE)

    override fun request(
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ): Flow<Item> = requestImpl(query, langFrom, langTo)

    @Suppress("TooGenericExceptionCaught")
    private fun requestImpl(
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ): Flow<Item> = flow {
        val queriesRes = formsForExamplesProvider.requestFor(
            query = query,
            langFrom = langFrom,
            langTo = langTo,
        )
        val queries = queriesRes.fold(
            { Log.e(TAG, it.e) { "No forms" }; listOf(query) },
            { it.map { it.text } }
        )

        val baseUrl = getUrlBase()
        for (query in queries) {
            val encodedTerm = encodePathSegment(query)
            val seen = HashSet<String>()
            for (corpus in textCorporaFor(langFrom)) {
                val url = "$baseUrl/$corpus/sentences/$encodedTerm"
                var hasNextPage = true
                var offset = 0
                while (hasNextPage) {
                    var response: LeipzigSentencesResponse? = null
                    while (response == null) {
                        response = try {
                            ktorClientHolder.client.get(url) {
                                parameter("offset", offset)
                                parameter("limit", LIMIT)
                            }.body<LeipzigSentencesResponse>()
                        } catch (e: Exception) {
                            emit(Item.Failure(Err.from(e)))
                            null
                        }
                    }
                    val pageDetails = mutableListOf<LexicalItemDetail>()
                    response.sentences.forEach {
                        if (!seen.contains(it.id)) {
                            seen.add(it.id)
                            pageDetails.add(it.toExample(langFrom))
                        }
                    }
                    if (pageDetails.isNotEmpty()) {
                        val result = Item.Page(
                            details = pageDetails,
                            nextPageTypes = types,
                            errors = queriesRes.fold({ listOf(it) }, { emptyList() }),
                        )
                        emit(result)
                    }
                    hasNextPage = response.sentences.size == LIMIT
                    offset += LIMIT
                }
            }
        }
    }

    private fun LeipzigSentence.toExample(lang: Lang) = LexicalItemDetail.Example(
        translationsSet = TranslationsSet(
            original = Sentence(
                text = this.sentence,
                lang = lang,
                source = this@WortschatzLeipzigLexicalItemDetailsSource.source,
            ),
            translations = emptyList(),
            translationsQualities = emptyList()
        ),
        source = this@WortschatzLeipzigLexicalItemDetailsSource.source,
    )

    private suspend fun getUrlBase(): String {
        return when (getKotlinPlatform()) {
            KotlinPlatform.JS -> {
                val base = backendAddressProvider.baseUrl.first()
                "$base/ws/sentences"
            }
            else -> "https://api.wortschatz-leipzig.de/ws/sentences"
        }
    }

    private companion object {
        const val LIMIT = 10
    }
}

private fun encodePathSegment(value: String): String = value.encodeURLPathPart()

private fun textCorporaFor(lang: Lang): List<String> {
    // curl -X 'GET' \
    //  'https://api.wortschatz-leipzig.de/ws/corpora' \
    //  -H 'accept: application/json' | jq
    return when (lang) {
        Lang.RU -> listOf(
            "rus_news_2013_1M",
        )
        Lang.EN -> listOf(
            "eng_wikipedia_2012_1M",
            "eng_news_2013_3M",
            "eng_news_2012_3M",
        )
        Lang.DE -> listOf(
            "deu_wikipedia_2010_1M",
            "deu_news_2012_3M",
            "deu_news_2012_1M",
            "deu_news_2010_100K",
            "deu_news_2008_100K",
        )
        Lang.FR -> listOf(
            "fra_news_2011_3M",
        )
    }
}

private const val TAG = "WortschatzLeipzigLexicalItemDetailsSource"
