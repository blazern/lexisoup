package blazern.lexisoup.feature.search_results

import arrow.core.Either
import blazern.lexisoup.data.translator.api.Translator
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
    override val capabilities: Flow<Translator.Capabilities> = flowOf(
        Translator.Capabilities(
            langs = Lang.entries.associateWith { Lang.entries.toSet() },
            textLengthMax = 100,
            textLengthMin = 10,
            translateBatchSizeLimit = 10,
        )
    ),
) : Translator {

    var capturedSentences = mutableListOf<List<Sentence>>()
        private set
    var capturedLangFrom = mutableListOf<Lang>()
        private set
    var capturedLangTo = mutableListOf<Lang>()
        private set

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
}
