package blazern.lexisoup.feature.search_results

import arrow.core.Either
import arrow.core.Either.Left
import blazern.lexisoup.data.translator.api.Translator
import blazern.lexisoup.data.translator.api.Translator.Capabilities
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

internal class FakeTranslator(
    private val results: List<Either<Err, TranslationsSet>> = emptyList(),
    override val source: DataSource = DataSource.Backend(impl = null),
    override val capabilities: Flow<Capabilities> = flowOf(
        Capabilities(
            availableLangs = Lang.entries.associateWith { Lang.entries.toSet() },
            downloadableLangs = emptyMap(),
            availableOffline = false,
            textLengthMax = 100,
            textLengthMin = 10,
            translateBatchSizeLimit = 10,
        )
    ),
    private val downloadLangsResult: Either<Err, Unit> = Left(Err.from(IllegalStateException()))
) : Translator {

    val capturedSentences = mutableListOf<List<Sentence>>()
    val capturedLangFrom = mutableListOf<Lang>()
    val capturedLangTo = mutableListOf<Lang>()
    val capturedDownloadRequests = mutableListOf<Pair<Lang, Lang>>()

    override fun translate(
        sentences: List<Sentence>,
        langFrom: Lang,
        langTo: Lang,
    ): Flow<Either<Err, TranslationsSet>> = flow {
        capturedSentences.add(sentences)
        capturedLangFrom.add(langFrom)
        capturedLangTo.add(langTo)
        results.forEach { emit(it) }
    }

    override suspend fun downloadLangsPair(
        lang1: Lang,
        lang2: Lang
    ): Either<Err, Unit> {
        capturedDownloadRequests += lang1 to lang2
        return downloadLangsResult
    }
}

internal fun capabilities(
    availableLangs: Map<Lang, Set<Lang>> = Lang.entries.associateWith { Lang.entries.toSet() },
    downloadableLangs: Map<Lang, Set<Lang>> = emptyMap(),
    availableOffline: Boolean = false,
    textLengthMin: Int = 0,
    textLengthMax: Int = Int.MAX_VALUE,
    translateBatchSizeLimit: Int = Int.MAX_VALUE,
) = Capabilities(
    availableLangs,
    downloadableLangs,
    availableOffline,
    textLengthMin,
    textLengthMax,
    translateBatchSizeLimit,
)