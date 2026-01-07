package blazern.lexisoup.utils

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.coroutineContext

/**
 * NOTE: could contain tricky concurrency bugs
 */
@OptIn(ExperimentalAtomicApi::class)
class FlowIterator<T>
private constructor(
    private val signal: Channel<Unit>,
    private val values: Channel<Either<Throwable, T?>>,
    private val job: Job,
    private val hasEnded: AtomicBoolean,
) : AutoCloseable {

    private val mutex = Mutex()

    /**
     * @return null if the flow has finished
     * @throws Throwable - whatever the flow has thrown
     */
    suspend fun next(): T? = mutex.withLock {
        // If a previous consumer was cancelled after requesting,
        // the producer may have already placed the response here.
        values.tryReceive().getOrNull()?.let {
            return when (it) {
                is Left -> throw it.value
                is Right -> it.value
            }
        }

        if (hasEnded.load() == true) {
            return null
        }
        try {
            signal.send(Unit)
        } catch (_: ClosedSendChannelException) {
            hasEnded.store(true)
            return null
        }

        val r = when (val r = values.receive()) {
            is Left -> throw r.value
            is Right -> r.value
        }
        return r
    }

    /**
     * Returns true if the upstream is know to have finished.
     * Note that except for rare corner-cases this method will always
     * return `false` after the last element from the upstream has arrived
     * but `next` was not called yet.
     */
    fun hasEnded(): Boolean = hasEnded.load()

    override fun close() {
        hasEnded.store(true)
        job.cancel()
        signal.close()
        values.close()
    }

    companion object {
        /**
         * @param coroutineScope if not passed, the iterator will create a
         * collector job is a **child of the caller's coroutine**.
         */
        suspend operator fun <T> invoke(
            flow: Flow<T>,
            coroutineScope: CoroutineScope? = null,
        ): FlowIterator<T> {
            val signal = Channel<Unit>(Channel.RENDEZVOUS)
            val values = Channel<Either<Throwable, T?>>(Channel.BUFFERED)
            val hasEnded = AtomicBoolean(false)

            // Bind to the caller's context (implicit scope)
            val scope = coroutineScope ?: CoroutineScope(coroutineContext)

            @Suppress("TooGenericExceptionCaught")
            val job = scope.launch(start = CoroutineStart.UNDISPATCHED) {
                try {
                    signal.receive()
                    flow.collect {
                        values.send(Right(it))
                        signal.receive()
                    }
                    hasEnded.store(true)
                    values.trySend(Right(null))
                } catch (t: Throwable) {
                    values.trySend(Left(t))
                } finally {
                    values.close()
                    signal.close()
                }
            }

            return FlowIterator(signal, values, job, hasEnded)
        }
    }
}