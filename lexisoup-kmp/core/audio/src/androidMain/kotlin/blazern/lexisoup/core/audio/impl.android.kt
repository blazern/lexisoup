package blazern.lexisoup.core.audio

import android.content.Context
import androidx.core.net.toUri
import org.koin.core.context.GlobalContext

internal actual fun audioPlayerFor(
    urls: List<String>,
): AudioPlayer {
    if (urls.isEmpty()) error("Empty urls list passed to audioPlayerFor")
    // Android has only a limited support of ogg.
    val url = urls.firstOrNull { it.endsWith(".mp3") } ?: urls.first()
    return AndroidAudioPlayer(
        GlobalContext.get().get<Context>(),
        url.toUri(),
    )
}
