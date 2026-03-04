package blazern.lexisoup.feature.search_results.usecases

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import blazern.lexisoup.data.translator.api.Translator
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_BASIC
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_MAX
import blazern.lexisoup.domain.model.copy
import blazern.lexisoup.feature.search_results.FakeTranslator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TranslateDetailsUseCaseTest {

    private val forms = LexicalItemDetail.Forms(
        value = LexicalItemDetail.Forms.Value.Text("some forms"),
        lang = Lang.EN,
        source = DataSource.Kaikki,
    )

    private val translations = LexicalItemDetail.WordTranslations(
        translationsSet = TranslationsSet(
            Sentence("word", Lang.EN, DataSource.Kaikki),
            listOf(Sentence("Wort", Lang.DE, DataSource.Kaikki)),
            listOf(QUALITY_BASIC),
        ),
        source = DataSource.Kaikki,
    )

    private val synonyms = LexicalItemDetail.Synonyms(
        translationsSet = TranslationsSet(
            Sentence("phrase", Lang.EN, DataSource.Kaikki),
        ),
        source = DataSource.Kaikki,
    )


    private val useCase = TranslateDetailsUseCaseImpl(
        canTranslate = CanTranslateUseCase()
    )

    @Test
    fun `all success`() = runTest {
        val originalsAndTranslations = mapOf(
            sentence("this is sentence 1") to sentence("das ist eine Übersetzung 1"),
            sentence("this is sentence 2") to sentence("das ist eine Übersetzung 2"),
            sentence("this is sentence 3") to sentence("das ist eine Übersetzung 3"),
        )
        val originals = originalsAndTranslations.keys.toList()
        val details = LexicalItemDetail.Type.entries.map { type ->
            when (type) {
                LexicalItemDetail.Type.EXPLANATION -> LexicalItemDetail.Explanation(
                    translationsSet = TranslationsSet(
                        original = originals[0],
                    ),
                    source = DataSource.Kaikki,
                )
                LexicalItemDetail.Type.EXAMPLE -> LexicalItemDetail.Example(
                    translationsSet = TranslationsSet(
                        original = originals[1],
                    ),
                    source = DataSource.Kaikki,
                )
                LexicalItemDetail.Type.ETYMOLOGY -> LexicalItemDetail.Etymology(
                    translationsSet = TranslationsSet(
                        original = originals[2],
                    ),
                    source = DataSource.Kaikki,
                )
                LexicalItemDetail.Type.FORMS -> forms // Not translatable
                LexicalItemDetail.Type.WORD_TRANSLATIONS -> translations // Not translatable
                LexicalItemDetail.Type.SYNONYMS -> synonyms // Not translatable
            }
        }

        val translations = originals.mapIndexed { index, sentence ->
            TranslationsSet(
                original = sentence,
                translations = listOf(
                    originalsAndTranslations[sentence]!!,
                ),
                translationsQualities = listOf(QUALITY_BASIC),
            )
        }

        val translator = FakeTranslator(
            results = translations.map { Right(it) },
            capabilities = flowOf(Translator.Capabilities(
                langs = Lang.entries.associateWith { Lang.entries.toSet() },
                textLengthMax = 1000,
                textLengthMin = 0,
                translateBatchSizeLimit = 100,
            ))
        )

        val result = useCase(
            details = details,
            translator = translator,
            langFrom = Lang.EN,
            langTo = Lang.DE,
        ).toList()

        assertEquals(listOf(Lang.EN), translator.capturedLangFrom)
        assertEquals(listOf(Lang.DE), translator.capturedLangTo)
        assertEquals(listOf(originals), translator.capturedSentences)

        val expected = details.map { detail ->
            when (detail) {
                is LexicalItemDetail.Etymology -> detail.copy(
                    translationsSet = detail.translationsSet.copy(
                        translations = listOf(originalsAndTranslations[detail.translationsSet.original]!!),
                        translationsQualities = listOf(QUALITY_BASIC),
                    )
                )
                is LexicalItemDetail.Example -> detail.copy(
                    translationsSet = detail.translationsSet.copy(
                        translations = listOf(originalsAndTranslations[detail.translationsSet.original]!!),
                        translationsQualities = listOf(QUALITY_BASIC),
                    )
                )
                is LexicalItemDetail.Explanation -> detail.copy(
                    translationsSet = detail.translationsSet.copy(
                        translations = listOf(originalsAndTranslations[detail.translationsSet.original]!!),
                        translationsQualities = listOf(QUALITY_BASIC),
                    )
                )
                is LexicalItemDetail.Forms -> detail
                is LexicalItemDetail.Synonyms -> detail
                is LexicalItemDetail.WordTranslations -> detail
            }
        }.map { Right(it) }

        assertEquals(expected, result)
    }

    @Test
    fun `emits Left error and stops without emitting any Right details`() = runTest {
        val original = Sentence("some text some text some text", Lang.EN, DataSource.Kaikki)

        val detail = LexicalItemDetail.Example(
            translationsSet = TranslationsSet(
                original = original,
                translations = emptyList(),
                translationsQualities = emptyList(),
            ),
            source = DataSource.Kaikki,
        )

        val ok = translationsSet(original = original, translatedText = "ok", langTo = Lang.DE)
        val err = Err.Other(null)

        val translator = FakeTranslator(
            results = listOf(
                Left(err),
                Right(ok),
            )
        )

        val result = useCase(
            details = listOf(detail),
            translator = translator,
            langFrom = Lang.EN,
            langTo = Lang.DE,
        ).toList()

        // Once an error is observed, the use case emits the error(s) and returns early.
        assertEquals(listOf(Left(err)), result)
    }

    private fun translationsSet(
        original: Sentence,
        translatedText: String,
        langTo: Lang,
    ): TranslationsSet {
        val source = original.source
        return TranslationsSet(
            original = original,
            translations = listOf(Sentence(translatedText, langTo, source)),
            translationsQualities = listOf(QUALITY_MAX),
        )
    }

    private fun sentence(text: String) = Sentence(text, Lang.EN, DataSource.Kaikki)
}