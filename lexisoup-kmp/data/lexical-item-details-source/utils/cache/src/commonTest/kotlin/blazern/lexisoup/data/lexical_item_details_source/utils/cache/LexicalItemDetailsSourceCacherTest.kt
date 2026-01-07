package blazern.lexisoup.data.lexical_item_details_source.utils.cache

import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch

@OptIn(ExperimentalAtomicApi::class)
class LexicalItemDetailsSourceCacherTest {
    private val source = DataSource.PANLEX
    private val details = listOf(
        LexicalItemDetail.Explanation("1", source),
        LexicalItemDetail.Explanation("2", source),
        LexicalItemDetail.Explanation("3", source),
    )
    private val detailsFlow: Flow<Item> =
        details.map { Item.Page(listOf(it), setOf(LexicalItemDetail.Type.EXAMPLE)) }.asFlow()

    @Test
    fun `happy path`() = runTest {
        val cacher = LexicalItemDetailsSourceCacher()
        val executeCalls = AtomicInt(0)
        val execute = {
            executeCalls.incrementAndFetch()
            detailsFlow
        }

        val received = mutableListOf<Item>()

        cacher
            .retrieveOrExecute(source, "query", Lang.EN, Lang.DE) { execute() }
            .collect { received += it }

        assertEquals(1, executeCalls.load())
        assertEquals(detailsFlow.toList(), received)
    }

    @Test
    fun `caches only successes and replays them to late subscriber, execute called once`() =
        runTest {
            val cacher = LexicalItemDetailsSourceCacher()

            val executeCalls = AtomicInt(0)
            val execute = {
                executeCalls.incrementAndFetch()
                detailsFlow
            }

            val received1 = cacher
                .retrieveOrExecute(source, "query", Lang.DE, Lang.EN) { execute() }
                .toList()
            val received2 = cacher
                .retrieveOrExecute(source, "query", Lang.DE, Lang.EN) { execute() }
                .toList()

            assertEquals(1, executeCalls.load())
            assertEquals(detailsFlow.toList(), received1)
            assertEquals(detailsFlow.toList(), received2)
        }

    @Test
    fun `new stream for different request - execute called per distinct request`() = runTest {
        val cacher = LexicalItemDetailsSourceCacher()
        val executeCalls = AtomicInt(0)
        val execute = {
            executeCalls.incrementAndFetch()
            detailsFlow
        }

        val baseSource = source
        val otherSource = enumValues<DataSource>().first { it != baseSource }

        // Changing every item of the request
        val fn = suspend {
            cacher.retrieveOrExecute(baseSource, "q", Lang.EN, Lang.DE) { execute() }.toList()
            cacher.retrieveOrExecute(baseSource, "q2", Lang.EN, Lang.DE) { execute() }.toList()
            cacher.retrieveOrExecute(baseSource, "q", Lang.DE, Lang.DE) { execute() }.toList()
            cacher.retrieveOrExecute(baseSource, "q", Lang.EN, Lang.EN) { execute() }.toList()
            cacher.retrieveOrExecute(otherSource, "q", Lang.EN, Lang.DE) { execute() }.toList()
        }
        fn()

        // One execute() per distinct request
        assertEquals(5, executeCalls.load())

        // Let's do every single same request once more and ensure
        // that this time they are not repeated
        fn()
        assertEquals(5, executeCalls.load())
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `late subscriber replays cached items immediately`() = runTest {
        val cacher = LexicalItemDetailsSourceCacher()

        // long-running upstream
        val emitted = Channel<LexicalItemDetail>(Channel.Factory.UNLIMITED)
        val executeCalls = AtomicInt(0)
        val execute = {
            executeCalls.incrementAndFetch()
            flow {
                for (v in emitted) emit(Item.Page(listOf(v), setOf(LexicalItemDetail.Type.EXAMPLE)))
            }
        }

        // Start first collector to populate the cache as upstream emits
        val received1 = mutableListOf<Item>()
        val job1 = launch {
            cacher.retrieveOrExecute(source, "q", Lang.EN, Lang.DE) { execute() }
                .collect { received1 += it }
        }

        // Upstream produces two items
        emitted.send(details[0])
        emitted.send(details[1])
        runCurrent()

        // Start a late subscriber that only asks for the first two items
        val late = async {
            cacher.retrieveOrExecute(source, "q", Lang.EN, Lang.DE) { execute() }
                .take(2)
                .toList()
        }
        runCurrent()
        val fromLate = late.await()

        // execute() called once, late subscriber got the two cached items instantly
        assertEquals(1, executeCalls.load())
        assertEquals(detailsFlow.take(2).toList(), fromLate)

        // If upstream keeps going, first collector continues live
        emitted.send(details[2])
        runCurrent()
        assertEquals(detailsFlow.take(3).toList(), received1)

        job1.cancelAndJoin()
        emitted.close()
    }

    @Test
    fun `forwards Left promptly but does NOT replay errors to late subscribers`() = runTest {
        val cacher = LexicalItemDetailsSourceCacher()
        val e1 = Exception()
        val e2 = Exception()

        val executeCalls = AtomicInt(0)
        val execute = {
            executeCalls.incrementAndFetch()
            flow {
                emit(Item.Failure(Err.from(e1)))
                emit(Item.Page(listOf(details[0]), setOf(LexicalItemDetail.Type.EXAMPLE)))
                emit(Item.Failure(Err.from(e2)))
                emit(Item.Page(listOf(details[1]), setOf(LexicalItemDetail.Type.EXAMPLE)))
            }
        }

        val first = cacher.retrieveOrExecute(source, "q", Lang.EN, Lang.DE) { execute() }.toList()
        val second = cacher.retrieveOrExecute(source, "q", Lang.EN, Lang.DE) { execute() }.toList()

        assertEquals(1, executeCalls.load())
        assertEquals(
            listOf(
                Item.Failure(Err.from(e1)),
                Item.Page(listOf(details[0]), setOf(LexicalItemDetail.Type.EXAMPLE)),
                Item.Failure(Err.from(e2)),
                Item.Page(listOf(details[1]), setOf(LexicalItemDetail.Type.EXAMPLE))
            ), first
        )
        // no errors replayed
        assertEquals(
            listOf(
                Item.Page(listOf(details[0]), setOf(LexicalItemDetail.Type.EXAMPLE)),
                Item.Page(listOf(details[1]), setOf(LexicalItemDetail.Type.EXAMPLE))
            ),
            second
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `slow subscriber does NOT block other subscribers`() = runTest {
        val cacher = LexicalItemDetailsSourceCacher()

        // controllable long-running upstream
        val upstream = Channel<Item>(Channel.Factory.UNLIMITED)
        val execute = {
            flow {
                for (x in upstream) {
                    emit(x)
                }
            }
        }

        val blockFirstAfterFirstItem = Channel<Unit>(Channel.Factory.RENDEZVOUS)
        val releaseFirst = Channel<Unit>(Channel.Factory.RENDEZVOUS)

        val results1 = mutableListOf<Item>()
        val results2 = mutableListOf<Item>()

        // Slow collector: blocks after receiving first emission
        val job1 = launch {
            cacher.retrieveOrExecute(source, "q", Lang.EN, Lang.DE) { execute() }.collect {
                results1 += it
                if (results1.size == 1) {
                    // signal we hit first emit
                    blockFirstAfterFirstItem.send(Unit)
                    // wait until released
                    releaseFirst.receive()
                }
            }
        }

        // Emit first item, job1 will receive it and then block.
        upstream.send(Item.Page(listOf(details[0]), setOf(LexicalItemDetail.Type.EXAMPLE)))
        runCurrent()
        // Wait until job1 has collected the first item
        blockFirstAfterFirstItem.receive()

        // Fast collector
        val job2 = launch {
            cacher.retrieveOrExecute(source, "q", Lang.EN, Lang.DE) { execute() }.collect {
                results2 += it
            }
        }
        runCurrent()
        // Fast collector should have received first detail
        assertEquals(detailsFlow.take(1).toList(), results2)

        // Emit second item; fast collector should advance even while job1 is blocked
        upstream.send(Item.Page(listOf(details[1]), setOf(LexicalItemDetail.Type.EXAMPLE)))
        runCurrent()
        assertEquals(detailsFlow.take(2).toList(), results2)

        // Let the slow collector resume and catch up
        releaseFirst.send(Unit)
        runCurrent()
        assertEquals(detailsFlow.take(2).toList(), results1)

        job1.cancelAndJoin()
        job2.cancelAndJoin()
        upstream.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `cancellation mid-stream - late subscriber replays successes and continues live`() =
        runTest {
            val cacher = LexicalItemDetailsSourceCacher()

            val upstream = Channel<Item>(Channel.Factory.UNLIMITED)
            val executeCalls = AtomicInt(0)
            val execute = {
                executeCalls.incrementAndFetch()
                flow {
                    for (x in upstream) emit(x)
                }
            }

            val parentScope = this

            // First collector consumes some data, then cancels
            val results1 = mutableListOf<Item>()
            val job1 = launch {
                cacher.retrieveOrExecute(source, "q", Lang.EN, Lang.DE, parentScope) { execute() }
                    .collect { results1 += it }
            }

            upstream.send(Item.Page(listOf(details[0]), setOf(LexicalItemDetail.Type.EXAMPLE)))
            runCurrent()
            assertEquals(detailsFlow.take(1).toList(), results1)

            job1.cancelAndJoin()

            // Late subscriber: should replay first detail immediately, then continue to get others
            val results2 = mutableListOf<Item>()
            val job2 = launch {
                cacher.retrieveOrExecute(source, "q", Lang.EN, Lang.DE, parentScope) { execute() }
                    .collect { results2 += it }
            }
            runCurrent()
            assertEquals(detailsFlow.take(1).toList(), results2)

            upstream.send(Item.Page(listOf(details[1]), setOf(LexicalItemDetail.Type.EXAMPLE)))
            upstream.send(Item.Page(listOf(details[2]), setOf(LexicalItemDetail.Type.EXAMPLE)))
            runCurrent()

            assertEquals(detailsFlow.take(1).toList(), results1)
            assertEquals(detailsFlow.take(3).toList(), results2)
            assertEquals(1, executeCalls.load())

            upstream.close()
            job2.cancelAndJoin()
        }
}
