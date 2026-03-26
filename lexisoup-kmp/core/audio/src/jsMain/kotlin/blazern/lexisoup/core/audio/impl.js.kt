package blazern.lexisoup.core.audio

internal actual fun audioPlayerFor(
    urls: List<String>,
): AudioPlayer {
    if (urls.isEmpty()) error("Empty urls list passed to audioPlayerFor")
    return JsAudioPlayer(urls)
}
