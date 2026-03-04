package blazern.lexisoup.feature.search_results.usecases

import blazern.lexisoup.data.translator.api.Translator
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.feature.search_results.FakeTranslator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CanTranslateUseCaseTest {

    private val useCase = CanTranslateUseCase()

    @Test
    fun `returns false when detail has no translatableSet`() = runTest {
        val translator = FakeTranslator()

        // Synonyms are not translatable
        val nonTranslatable = LexicalItemDetail.Synonyms(
            listOf(Sentence("hound", Lang.EN, DataSource.Kaikki)),
            DataSource.Kaikki,
        )

        val result = useCase(
            detail = nonTranslatable,
            translator = translator,
            langFrom = Lang.EN,
            langTo = Lang.DE,
        )

        assertFalse(result)
    }

    @Test
    fun `returns false when original lang does not match langFrom`() = runTest {
        val translator = FakeTranslator()

        val detail = LexicalItemDetail.Example(
            TranslationsSet(
                original = Sentence("der hund ist nett", Lang.DE, DataSource.Kaikki),
                translations = emptyList(),
                translationsQualities = emptyList(),
            ),
            DataSource.Kaikki,
        )

        val result = useCase(
            detail = detail,
            translator = translator,
            langFrom = Lang.EN, // mismatch (original is DE)
            langTo = Lang.RU,
        )

        assertFalse(result)
    }

    @Test
    fun `returns true when translatable`() = runTest {
        val detail = LexicalItemDetail.Example(
            TranslationsSet(
                original = Sentence("the hound is nice", Lang.EN, DataSource.Kaikki),
                translations = emptyList(),
                translationsQualities = emptyList(),
            ),
            DataSource.Kaikki,
        )

        val translator = FakeTranslator(
            capabilities = flowOf(
                Translator.Capabilities(
                    langs = mapOf(Lang.EN to setOf(Lang.DE)),
                    textLengthMax = 100,
                    textLengthMin = 0,
                    translateBatchSizeLimit = 10,
                )
            )
        )

        val result = useCase(
            detail = detail,
            translator = translator,
            langFrom = Lang.EN,
            langTo = Lang.DE,
        )

        assertTrue(result)
    }

    @Test
    fun `returns false when translatable, lang matches, but translator canTranslate is false`() = runTest {
        val detail = LexicalItemDetail.Example(
            TranslationsSet(
                original = Sentence("the hound is nice", Lang.EN, DataSource.Kaikki),
                translations = emptyList(),
                translationsQualities = emptyList(),
            ),
            DataSource.Kaikki,
        )

        // Make canTranslate() fail via capabilities (unsupported lang pair).
        val translator = FakeTranslator(
            capabilities = flowOf(
                Translator.Capabilities(
                    langs = mapOf(Lang.EN to setOf(Lang.FR)),
                    textLengthMax = 100,
                    textLengthMin = 0,
                    translateBatchSizeLimit = 10,
                )
            )
        )

        val result = useCase(
            detail = detail,
            translator = translator,
            langFrom = Lang.EN,
            langTo = Lang.DE,
        )

        assertFalse(result)
    }
}