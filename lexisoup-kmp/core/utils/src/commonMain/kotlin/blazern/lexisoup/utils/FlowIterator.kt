package blazern.lexisoup.utils

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import blazern.lexisoup.core.logging.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

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
        ensureNotCancelled()

        // If a previous consumer was canceled after requesting,
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
            ensureNotCancelled()
            hasEnded.store(true)
            return null
        }

        val received = try {
            values.receive()
        } catch (_: ClosedReceiveChannelException) {
            ensureNotCancelled()
            hasEnded.store(true)
            return null
        }

        val result = when (received) {
            is Left -> throw received.value
            is Right -> received.value
        }
        return result
    }

    private suspend fun ensureNotCancelled() {
        if (job.isCancelled) {
            currentCoroutineContext().ensureActive()
            throw CancellationMisalignmentException()
        }
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
        signal.cancel()
        values.cancel()
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
            val scope = coroutineScope ?: CoroutineScope(currentCoroutineContext())

            // NOTE: [CoroutineStart.UNDISPATCHED] to start immediately and wait on [signal.receive]
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
                } catch (e: CancellationException) {
                    throw e
                } catch (t: Throwable) {
                    values.trySend(Left(t))
                } finally {
                    values.cancel()
                    signal.cancel()
                }
            }

            return FlowIterator(signal, values, job, hasEnded)
        }

        const val INVALID_CANCELLATION_MSG = "This FlowIterator's Job was canceled. " +
                "Please make sure to never use a FlowIterator from a scope with a lifetime not " +
                "aligned with the scope of this FlowIterator (e.g. iterator created for a screen " +
                "should not be used in a singleton), and after a `close` call."
        internal class CancellationMisalignmentException
            : IllegalStateException(INVALID_CANCELLATION_MSG)
    }
}
