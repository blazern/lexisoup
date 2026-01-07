package blazern.lexisoup.data.lexical_item_details_source.kaikki

import blazern.lexisoup.data.kaikki.KaikkiClient
import blazern.lexisoup.data.kaikki.model.Entry
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.data.lexical_item_details_source.utils.cache.LexicalItemDetailsSourceCacher
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

interface KaikkiLexicalItemDetailsSource : LexicalItemDetailsSource {
    override val source: DataSource
        get() = DataSource.KAIKKI
    override val types: Set<LexicalItemDetail.Type>
        get() = setOf(
            LexicalItemDetail.Type.FORMS,
            LexicalItemDetail.Type.EXPLANATION,
            LexicalItemDetail.Type.EXAMPLE,
        )
}

class KaikkiLexicalItemDetailsSourceImpl(
    private val kaikkiClient: KaikkiClient,
    private val cacher: LexicalItemDetailsSourceCacher,
) : KaikkiLexicalItemDetailsSource {

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
        langTo: Lang,
        depth: Int = 0,
    ): Flow<Item> {
        return flow {
            var entries: List<Entry>? = null
            do {
                entries = kaikkiClient.search(query, langFrom).fold(
                    { emit(Item.Failure(it)); null },
                    { it }
                )
            } while (entries == null)

            for (entry in entries) {
                val formsOf = entry.senses.map { it.formOf }.flatten()
                val purelyWordForm = formsOf.size == entry.senses.size
                if (!purelyWordForm) {
                    val details = entry.toDetails(langFrom, langTo)
                    val page = Item.Page(
                        details = details,
                        nextPageTypes = types,
                    )
                    emit(page)
                }
                if (depth == 0) {
                    formsOf.forEach {
                        emitAll(requestImpl(it.word, langFrom, langTo, depth + 1))
                    }
                }
            }
        }
    }
}
