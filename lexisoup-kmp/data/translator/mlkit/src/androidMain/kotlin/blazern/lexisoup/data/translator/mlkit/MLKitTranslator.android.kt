@file:Suppress("MatchingDeclarationName")

package blazern.lexisoup.data.translator.mlkit

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import blazern.lexisoup.core.logging.Log
import blazern.lexisoup.data.translator.api.Translator
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_BASIC
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap
import com.google.mlkit.nl.translate.Translator as MlKitTranslatorImpl

internal class MlKitTranslator : Translator {
    private val _capabilities = MutableStateFlow<Translator.Capabilities?>(null)
    private val impls = ConcurrentHashMap<Pair<Lang, Lang>, MlKitTranslatorImpl>()

    override val source = DataSource.MlKit
    override val capabilities: Flow<Translator.Capabilities>
        get() = flow {
            if (_capabilities.value == null) {
                _capabilities.value = getCurrentCapabilities()
            }
            emitAll(_capabilities.filterNotNull())
        }

    private suspend fun getCurrentCapabilities(): Translator.Capabilities {
        val models = RemoteModelManager.getInstance().getDownloadedModels()
        val langs = models
            .map { it.language }
            .mapNotNull { Lang.fromMlKit(it) }
            .toSet()
        Log.i(TAG) { "Downloaded langs: $langs" }
        return Translator.Capabilities(
            availableLangs = langs.associateWith { langs },
            downloadableLangs = Lang.entries.associateWith { Lang.entries.toSet() },
            availableOffline = true,
            textLengthMin = 0,
            textLengthMax = Int.MAX_VALUE,
            translateBatchSizeLimit = Int.MAX_VALUE,
        )
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun downloadLangsPair(
        lang1: Lang,
        lang2: Lang
    ): Either<Err, Unit> {
        return try {
            getMlKitTranslatorFor(lang1, lang2).download()
            _capabilities.value = getCurrentCapabilities()
            Right(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Left(Err.from(e))
        }
    }

    private fun getMlKitTranslatorFor(
        langFrom: Lang,
        langTo: Lang,
    ): MlKitTranslatorImpl = synchronized(this) {
        return impls.getOrPut(langFrom to langTo) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(langFrom.toMlKit())
                .setTargetLanguage(langTo.toMlKit())
                .build()
            Translation.getClient(options)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override fun translate(
        sentences: List<Sentence>,
        langFrom: Lang,
        langTo: Lang
    ): Flow<Either<Err, TranslationsSet>> = flow {
        val translator = getMlKitTranslatorFor(langFrom, langTo)
        sentences.forEach { sentence ->
            try {
                val translation = translator.translateSuspend(sentence.text)
                val translationSet = TranslationsSet(
                    original = sentence,
                    translations = listOf(
                        Sentence(translation, langTo, source)
                    ),
                    translationsQualities = listOf(QUALITY_BASIC),
                )
                emit(Right(translationSet))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emit(Left(Err.from(e)))
                return@flow
            }
        }
    }
}

actual fun platformSupportsMlKitTranslator() = true
actual fun createMlKitTranslator(): Translator = MlKitTranslator()

private const val TAG = "MlKitTranslator"
