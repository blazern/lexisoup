package blazern.lexisoup.feature.search_results.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blazern.lexisoup.core.audio.AudioPlayer
import blazern.lexisoup.core.ui.strings.stringResource
import blazern.lexisoup.core.ui.theme.LexisoupTheme
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.feature.search_results.repository.LocalAudiosPlaybackRepository
import lexisoup.core.ui.strings.generated.resources.search_results_cd_pause
import lexisoup.core.ui.strings.generated.resources.search_results_cd_play
import lexisoup.core.ui.strings.generated.resources.Res as ResStr


@Composable
fun AudioUI(
    audio: LexicalItemDetail.Pronunciation.Audio,
    modifier: Modifier = Modifier,
) {
    val player = LocalAudiosPlaybackRepository.current
    val state by player.playerStateOf(audio).collectAsState()
    val onClick = {
        when (state) {
            is AudioPlayer.State.NotPlaying -> player.play(audio)
            is AudioPlayer.State.Playing -> player.reset(audio)
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.clickable { onClick() }
    ) {
        IconButton(onClick = onClick) {
            when (state) {
                is AudioPlayer.State.NotPlaying -> Icon(
                    imageVector = Icons.Filled.PlayCircle,
                    contentDescription = stringResource(ResStr.string.search_results_cd_play),
                    tint = LocalContentColor.current,
                )
                is AudioPlayer.State.Playing -> Icon(
                    imageVector = Icons.Filled.PauseCircle,
                    contentDescription = stringResource(ResStr.string.search_results_cd_pause),
                    tint = LocalContentColor.current,
                )
            }
        }
        val name = audio.name
        if (name != null) {
            Text(name, Modifier.padding(end = 12.dp))
        }
    }
}

@Preview(widthDp = 150)
@Composable
private fun PreviewPaused() {
    LexisoupTheme {
        AudioUI(LexicalItemDetail.Pronunciation.Audio(
            "Katze.mp3",
            listOf("https://upload.wikimedia.org/wikipedia/commons/transcoded/4/40/De-Katze.ogg/De-Katze.ogg.mp3"),
            DataSource.Kaikki,
        ))
    }
}

@Preview(widthDp = 150)
@Composable
private fun PreviewPlaying() {
    val audio = LexicalItemDetail.Pronunciation.Audio(
        "Katze.mp3",
        listOf("https://upload.wikimedia.org/wikipedia/commons/transcoded/4/40/De-Katze.ogg/De-Katze.ogg.mp3"),
        DataSource.Kaikki,
    )
    val player = LocalAudiosPlaybackRepository.current
    player.play(audio)

    LexisoupTheme {
        AudioUI(audio)
    }
}
