package blazern.lexisoup.data.lexical_item_details_source.tatoeba

import arrow.core.Either
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.TranslationsSet

internal class FakeTatoebaClient : TatoebaClient {
    data class Call(
        val query: String,
        val langFrom: Lang,
        val langTo: Lang,
        val page: Int,
    )

    private data class Key(
        val query: String,
        val langFrom: Lang,
        val langTo: Lang,
        val page: Int,
    )

    val calls: MutableList<Call> = mutableListOf()

    private val scripted: MutableMap<Key, MutableList<Either<Err, List<TranslationsSet>>>> =
        mutableMapOf()

    fun enqueueResult(
        query: String,
        langFrom: Lang,
        langTo: Lang,
        page: Int,
        result: Either<Err, List<TranslationsSet>>,
    ) {
        val key = Key(query, langFrom, langTo, page)
        val list = scripted.getOrPut(key) { mutableListOf() }
        list += result
    }

    override suspend fun search(
        query: String,
        langFrom: Lang,
        langTo: Lang,
        page: Int,
    ): Either<Err, List<TranslationsSet>> {
        val call = Call(query, langFrom, langTo, page)
        calls += call

        val key = Key(query, langFrom, langTo, page)
        val queue = scripted[key]
            ?: error("No scripted results for $key")

        if (queue.isEmpty()) {
            error("No more scripted results for $key")
        }

        return queue.removeAt(0)
    }
}
