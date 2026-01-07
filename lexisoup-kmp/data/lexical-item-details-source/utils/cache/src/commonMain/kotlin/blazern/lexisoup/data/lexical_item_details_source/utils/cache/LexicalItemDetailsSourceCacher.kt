package blazern.lexisoup.data.lexical_item_details_source.utils.cache

import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.utils.FlowIterator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class LexicalItemDetailsSourceCacher {
    private val dataStreamsMutex = Mutex()
    private val dataStreams = mutableMapOf<DataKey, DataStream>()

    /**
     * Won't cache errors - every error it emits was just received from [execute].
     * Order of emitted items is not guaranteed to be kept, please order them manually.
     */
    @Suppress("TooGenericExceptionCaught")
    open fun retrieveOrExecute(
        source: DataSource,
        query: String,
        langFrom: Lang,
        langTo: Lang,
        coroutineScope: CoroutineScope? = null,
        execute: () -> Flow<Item>,
    ): Flow<Item> = flow {
        val key = DataKey(source, query, langFrom, langTo)
        val dataStream = dataStreamsMutex.withLock {
            dataStreams.getOrPut(key) {
                DataStream(
                    iterator = FlowIterator(execute(), coroutineScope),
                    receivedData = mutableListOf(),
                )
            }
        }

        var nextDataEntryIndex = 0
        while (true) {
            val newEntries = dataStream.dataMutex.withLock {
                if (nextDataEntryIndex < dataStream.receivedData.size) {
                    dataStream.receivedData.subList(
                        nextDataEntryIndex,
                        dataStream.receivedData.size,
                    ).toList()
                } else {
                    emptyList()
                }
            }
            // Each emit can suspend for long, so we're
            // emitting from outside of the lock
            if (newEntries.isNotEmpty()) {
                newEntries.forEach { emit(it) }
                nextDataEntryIndex += newEntries.size
                continue
            }

            val newResult = try {
                dataStream.iterator.next()
            } catch (e: Exception) {
                emit(Item.Failure(Err.from(e)))
                continue
            }
            val newItem = when (newResult) {
                is Item.Page -> newResult
                is Item.Failure -> {
                    emit(newResult)
                    null
                }
                null -> null
            }
            // NOTE: there's a rare race condition here:
            // - If there are 2 threads competing for this lock
            // - the first one has [newResult] and the second one has [null]
            // - if the one with [null] enters the lock first, it'll leave
            //   the flow before the second one will put the new value into [receivedData]
            // But it's not a huge problem, because for that to happen the [null] thread
            // must outrace the [newResult] thread, even though it's guaranteed to be behind
            // of it, because [iterator.next] blocks.
            dataStream.dataMutex.withLock {
                newItem?.let { dataStream.receivedData.add(it) }
                if (newResult == null && dataStream.receivedData.size <= nextDataEntryIndex) {
                    return@flow
                }
            }
        }
    }

    companion object {
        val NOOP = object : LexicalItemDetailsSourceCacher() {
            override fun retrieveOrExecute(
                source: DataSource,
                query: String,
                langFrom: Lang,
                langTo: Lang,
                coroutineScope: CoroutineScope?,
                execute: () -> Flow<Item>,
            ) = execute()
        }
    }
}

private data class DataKey(
    val source: DataSource,
    val query: String,
    val langFrom: Lang,
    val langTo: Lang,
)

private class DataStream(
    val iterator: FlowIterator<Item>,
    val receivedData: MutableList<Item.Page>,
    val dataMutex: Mutex = Mutex(),
)
