package blazern.lexisoup.feature.search_results.usecases

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_MAX
import blazern.lexisoup.feature.search_results.FakeTranslator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TranslateDetailsUseCaseTest {

    private val useCase = TranslateDetailsUseCaseImpl(
        canTranslate = CanTranslateUseCase()
    )

    @Test
    fun `all success`() = runTest {
        val original1 = Sentence("this is sentence 1", Lang.EN, DataSource.Kaikki)
        val original2 = Sentence("this is sentence 2", Lang.EN, DataSource.Kaikki)

        val detail1 = LexicalItemDetail.Explanation(
            translationsSet = TranslationsSet(
                original = original1,
                translations = emptyList(),
                translationsQualities = emptyList(),
            ),
            source = DataSource.Kaikki,
        )

        val detail2 = LexicalItemDetail.Synonyms(
            translationsSet = TranslationsSet(
                original = Sentence("not translated anyway", Lang.EN, DataSource.Kaikki),
                translations = emptyList(),
                translationsQualities = emptyList(),
            ),
            source = DataSource.Kaikki,
        )

        val detail3 = LexicalItemDetail.Example(
            translationsSet = TranslationsSet(
                original = original2,
                translations = emptyList(),
                translationsQualities = emptyList(),
            ),
            source = DataSource.Kaikki,
        )

        val translation1 = translationsSet(original = original1, translatedText = "dies ist satz 1", langTo = Lang.DE)
        val translation2 = translationsSet(original = original2, translatedText = "dies ist satz 2", langTo = Lang.DE)

        val translator = FakeTranslator(
            results = listOf(Right(translation1), Right(translation2))
        )

        val details = listOf(detail1, detail2, detail3)

        val result = useCase(
            details = details,
            translator = translator,
            langFrom = Lang.EN,
            langTo = Lang.DE,
        ).toList()

        // Only translatable details (Explanation + Example) should be sent to translator
        assertEquals(listOf(listOf(original1, original2)), translator.capturedSentences)
        assertEquals(listOf(Lang.EN), translator.capturedLangFrom)
        assertEquals(listOf(Lang.DE), translator.capturedLangTo)

        val expected = listOf<Either<Err, LexicalItemDetail>>(
            Right(detail1.copy(translationsSet = translation1)),
            Right(detail2),
            Right(detail3.copy(translationsSet = translation2)),
        )
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
}