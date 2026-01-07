package blazern.lexisoup.data.lexical_item_details_source.kaikki

import arrow.core.Either
import blazern.lexisoup.data.kaikki.KaikkiClient
import blazern.lexisoup.data.kaikki.model.Entry
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.Lang

class FakeKaikkiClient : KaikkiClient {

    private val responses: MutableMap<Pair<String, Lang>, ArrayDeque<Either<Err, List<Entry>>>> =
        mutableMapOf()

    /**
     * Enqueue one or more responses for a given (query, langFrom) pair.
     * They will be returned in the order they were added.
     */
    fun enqueue(
        query: String,
        langFrom: Lang,
        vararg results: Either<Err, List<Entry>>
    ) {
        val key = query to langFrom
        val queue = responses.getOrPut(key) { ArrayDeque() }
        for (result in results) {
            queue.addLast(result)
        }
    }

    override suspend fun search(
        query: String,
        langFrom: Lang
    ): Either<Err, List<Entry>> {
        val key = query to langFrom
        val queue = responses[key]
            ?: error("No fake response enqueued for query='$query', lang=$langFrom")

        if (queue.isEmpty()) {
            error("No more fake responses left for query='$query', lang=$langFrom")
        }

        return queue.removeFirst()
    }
}
