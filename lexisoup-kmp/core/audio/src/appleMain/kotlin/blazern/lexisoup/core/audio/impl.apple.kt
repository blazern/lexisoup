package blazern.lexisoup.core.audio

import blazern.lexisoup.core.logging.Log
import platform.Foundation.NSURL

internal actual fun audioPlayerFor(
    urls: List<String>,
): AudioPlayer {
    if (urls.isEmpty()) error("Empty urls list passed to audioPlayerFor")

    val urlString = urls.firstOrNull { it.endsWith(".mp3") } ?: urls.first()
    val url = NSURL.URLWithString(urlString)
    if (url == null) {
        Log.e("audioPlayerFor") { "Invalid URL passed to audioPlayerFor: $urlString" }
    }

    return IosAudioPlayer(url)
}
