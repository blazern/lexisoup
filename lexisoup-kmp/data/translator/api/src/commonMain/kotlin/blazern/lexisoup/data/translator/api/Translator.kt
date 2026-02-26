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
     * The returned Flow can however be shorter than [sentences] is an error has occurred, in that
     * case the last element of the Flow would be that error.
     * No more than 1 error will be emitted.
     */
    fun translate(
        sentences: List<Sentence>,
        langFrom: Lang,
        langTo: Lang,
    ): Flow<Either<Err, TranslationsSet>>

    suspend fun canTranslate(sentence: Sentence, langFrom: Lang, langTo: Lang): Boolean {
        val capabilities = capabilities.first()
        if (sentence.text.length !in capabilities.textLengthMin..capabilities.textLengthMax) {
            return false
        }
        if (!capabilities.langs[langFrom].orEmpty().contains(langTo)) {
            return false
        }
        return true
    }

    /**
     * @property langs supported language pairs.
     * @property translateBatchSizeLimit the maximum size of `texts` in [translate].
     */
    data class Capabilities(
        val langs: Map<Lang, Set<Lang>>,
        val textLengthMax: Int,
        val textLengthMin: Int,
        val translateBatchSizeLimit: Int,
    )
}
