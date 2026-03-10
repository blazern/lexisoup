package blazern.lexisoup.data.translator.api

import arrow.core.Either
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface Translator {
    val source: DataSource
    val capabilities: Flow<Capabilities>

    /**
     * @return the emitted values are guaranteed to match the indexes of [sentences] 1 to 1.
     * The returned Flow can however be shorter than [sentences] if an error has occurred, in that
     * case the last element of the Flow would be that error.
     * No more than 1 error will be emitted.
     */
    fun translate(
        sentences: List<Sentence>,
        langFrom: Lang,
        langTo: Lang,
    ): Flow<Either<Err, TranslationsSet>>

    suspend fun downloadLangsPair(
        lang1: Lang,
        lang2: Lang,
    ): Either<Err, Unit>

    /**
     * Can translate IF the [langFrom]-[langTo] pair is downloaded.
     */
    suspend fun canTranslate(
        sentence: Sentence,
        langFrom: Lang,
        langTo: Lang,
        ifLangsDownloaded: Boolean,
    ): Boolean {
        val capabilities = capabilities.first()
        if (sentence.text.length !in capabilities.textLengthMin..capabilities.textLengthMax) {
            return false
        }
        val hasLangs = { map: Map<Lang, Set<Lang>> ->
            map[langFrom].orEmpty().contains(langTo)
        }
        if (hasLangs(capabilities.availableLangs)) {
            return true
        }
        if (ifLangsDownloaded && hasLangs(capabilities.downloadableLangs)) {
            return true
        }
        return false
    }

    /**
     * @property availableLangs language pairs available right now.
     * @property downloadableLangs downloadable language pairs.
     * @property translateBatchSizeLimit the maximum size of `sentences` in [translate].
     */
    data class Capabilities(
        val availableLangs: Map<Lang, Set<Lang>>,
        val downloadableLangs: Map<Lang, Set<Lang>>,
        val availableOffline: Boolean,
        val textLengthMin: Int,
        val textLengthMax: Int,
        val translateBatchSizeLimit: Int,
    )
}
