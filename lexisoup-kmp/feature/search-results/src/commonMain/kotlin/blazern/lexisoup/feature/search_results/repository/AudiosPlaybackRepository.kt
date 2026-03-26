package blazern.lexisoup.feature.search_results.repository

import androidx.compose.runtime.staticCompositionLocalOf
import blazern.lexisoup.core.audio.AudioPlayer
import blazern.lexisoup.domain.model.LexicalItemDetail.Pronunciation.Audio
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal interface AudiosPlaybackRepository {
    fun playerStateOf(audio: Audio): StateFlow<AudioPlayer.State>
    fun play(audio: Audio)
    fun reset(audio: Audio)
}

internal class AudiosPlaybackRepositoryImpl(
    private val errorsRepo: BackgroundErrorsRepository,
    private val scope: CoroutineScope,
) : AudiosPlaybackRepository {
    private val audioPlayers = mutableMapOf<Audio, AudioPlayer>()

    init {
        val job = scope.coroutineContext[Job]
        job!!.invokeOnCompletion {
            audioPlayers.values.forEach {
                it.release()
            }
        }
    }

    override fun playerStateOf(audio: Audio): StateFlow<AudioPlayer.State> =
        playerFor(audio).state

    override fun play(audio: Audio) {
        scope.launch {
            playerFor(audio).play().onLeft {
                errorsRepo.emit(it)
            }
        }
    }

    override fun reset(audio: Audio) {
        scope.launch {
            playerFor(audio).reset().onLeft {
                errorsRepo.emit(it)
            }
        }
    }

    private fun playerFor(audio: Audio) = audioPlayers.getOrPut(audio) {
        AudioPlayer(audio.urls)
    }
}

internal val LocalAudiosPlaybackRepository = staticCompositionLocalOf<AudiosPlaybackRepository> {
    // For previews
    object : AudiosPlaybackRepository {
        val state = MutableStateFlow<AudioPlayer.State>(AudioPlayer.State.NotPlaying())

        override fun playerStateOf(audio: Audio) = state.asStateFlow()

        override fun play(audio: Audio) = state.update { AudioPlayer.State.Playing() }

        override fun reset(audio: Audio) = throw NotImplementedError()
    }
}
