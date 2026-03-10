package blazern.lexisoup.data.translator.api

import blazern.lexisoup.data.translator.api.Translator.Capabilities
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.Sentence
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TranslatorTest {
    @Test
    fun `can translate without downloading`() = runTest {
        val translator = FakeTranslator(capabilities(
            availableLangs = Lang.entries.associateWith { Lang.entries.toSet() },
        ))

        assertTrue(translator.canTranslate(
            Sentence("text", Lang.EN, DataSource.Kaikki),
            Lang.EN,
            Lang.DE,
            ifLangsDownloaded = false,
        ))
        assertTrue(translator.canTranslate(
            Sentence("text", Lang.EN, DataSource.Kaikki),
            Lang.EN,
            Lang.DE,
            ifLangsDownloaded = true,
        ))
    }

    @Test
    fun `can translate with downloading`() = runTest {
        val translator = FakeTranslator(capabilities(
            availableLangs = emptyMap(),
            downloadableLangs = Lang.entries.associateWith { Lang.entries.toSet() }
        ))

        assertFalse(translator.canTranslate(
            Sentence("text", Lang.EN, DataSource.Kaikki),
            Lang.EN,
            Lang.DE,
            ifLangsDownloaded = false,
        ))
        assertTrue(translator.canTranslate(
            Sentence("text", Lang.EN, DataSource.Kaikki),
            Lang.EN,
            Lang.DE,
            ifLangsDownloaded = true,
        ))
    }

    @Test
    fun `langs do not match`() = runTest {
        val translator = FakeTranslator(capabilities(
            availableLangs = mapOf(
                Lang.EN to setOf(Lang.DE)
            ),
        ))

        assertTrue(translator.canTranslate(
            Sentence("text", Lang.EN, DataSource.Kaikki),
            Lang.EN,
            Lang.DE,
            ifLangsDownloaded = false,
        ))
        assertFalse(translator.canTranslate(
            Sentence("text", Lang.EN, DataSource.Kaikki),
            Lang.DE,
            Lang.EN,
            ifLangsDownloaded = false,
        ))
    }

    @Test
    fun `lengths do not match`() = runTest {
        val textLengthMin = 3
        val textLengthMax = 5
        val translator = FakeTranslator(capabilities(
            textLengthMin = textLengthMin,
            textLengthMax = textLengthMax,
        ))

        for (size in (textLengthMin-1) .. (textLengthMax+1)) {
            val canTranslate = translator.canTranslate(
                Sentence("a".repeat(size), Lang.EN, DataSource.Kaikki),
                Lang.EN,
                Lang.DE,
                ifLangsDownloaded = false,
            )
            val expected = (textLengthMin..textLengthMax).contains(size)
            assertEquals(expected, canTranslate, "Size: $size")
        }
    }
}

private class FakeTranslator(
    capabilities: Capabilities,
) : Translator {
    override val source = DataSource.DeepL
    override val capabilities = flowOf(capabilities)

    override fun translate(
        sentences: List<Sentence>,
        langFrom: Lang,
        langTo: Lang
    ) = throw NotImplementedError()

    override suspend fun downloadLangsPair(
        lang1: Lang,
        lang2: Lang
    ) = throw NotImplementedError()
}

private fun capabilities(
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
