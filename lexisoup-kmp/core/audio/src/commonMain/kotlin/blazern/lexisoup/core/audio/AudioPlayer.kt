package blazern.lexisoup.core.audio

import arrow.core.Either
import blazern.lexisoup.domain.error.Err
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents a platform-specific player, which is created to play an audio located
 * at a specific URL.
 */
interface AudioPlayer {
    val state: StateFlow<State>

    /**
     * Suspends until the playback has started.
     */
    suspend fun play(): Either<Err, Unit>

    /**
     * Suspends until the player is reset to the beginning of the audio.
     */
    suspend fun reset(): Either<Err, Unit>

    /**
     * Releases all resources that the player has allocated.
     */
    fun release()

    sealed interface State {
        class NotPlaying : State
        class Playing : State
    }

    companion object {
        /**
         * NOTE:
         * @param urls the URLS the created player can choose from (on some platform specific
         * audio formats can be preferred.
         */
        operator fun invoke(
            urls: List<String>,
        ): AudioPlayer = audioPlayerFor(urls)
    }
}
