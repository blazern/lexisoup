@file:Suppress("TooGenericExceptionCaught")

package blazern.lexisoup.core.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import arrow.core.Either
import blazern.lexisoup.core.logging.Log
import blazern.lexisoup.domain.error.Err
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class AndroidAudioPlayer(
    context: Context,
    private val uri: Uri,
) : AudioPlayer {

    private val context = context.applicationContext
    private val mutex = Mutex()

    @Volatile
    private var released = false

    private var _impl: MediaPlayer? = null
    private val impl: MediaPlayer
        get() {
            // NOTE: MediaPlayer.create can block on IO operations
            val value = _impl ?: MediaPlayer.create(context, uri)?.apply {
                setOnCompletionListener {
                    _state.update { AudioPlayer.State.NotPlaying() }
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG) { "Unexpected error, what: $what, extra: $extra" }
                    // Returning false so that setOnCompletionListener would be called
                    false
                }
            }
            _impl = value
            // NOTE: if `value` is `null`, it could be because the network is not available.
            // In that case the next call of [impl] would try to create the player again,
            // which is exactly what we want, because the connection could be available by then.
            return value ?: error("MediaPlayer.create returned null for $uri")
        }

    private val _state = MutableStateFlow<AudioPlayer.State>(AudioPlayer.State.NotPlaying())
    override val state = _state.asStateFlow()

    override suspend fun play(): Either<Err, Unit> = withContext(Dispatchers.IO) {
        if (released) error("play: the player was released")
        try {
            mutex.withLock { impl.start() }
            _state.update { AudioPlayer.State.Playing() }
            Either.Right(Unit)
        } catch (e: Exception) {
            Either.Left(Err.from(e))
        }
    }

    override suspend fun reset(): Either<Err, Unit> = withContext(Dispatchers.IO) {
        if (released) error("play: the player was released")
        try {
            mutex.withLock {
                impl.pause()
                impl.seekTo(0)
            }
            _state.update { AudioPlayer.State.NotPlaying() }
            Either.Right(Unit)
        } catch (e: Exception) {
            Either.Left(Err.from(e))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun release() {
        released = true
        _state.update { AudioPlayer.State.NotPlaying() }
        // We need to avoid the memory leak. We don't have to make [release] suspend because
        // we don't care if the caller waits for the release or not.
        GlobalScope.launch(Dispatchers.IO) {
            mutex.withLock {
                _impl?.release()
                _impl = null
            }
        }
    }

    private companion object {
        const val TAG = "AndroidAudioPlayer"
    }
}
