package blazern.lexisoup.core.audio

/**
 * Please make sure this function never throws (unless a catastrophic failure has happened,
 * like an empty list is passed).
 * Instead, the platform implementations should return errors on [AudioPlayer.play],
 * [AudioPlayer.reset] and any other future player-related functions.
 */
internal expect fun audioPlayerFor(
    urls: List<String>,
): AudioPlayer
