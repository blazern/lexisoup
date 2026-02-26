package blazern.lexisoup.feature.search_results.usecases

import blazern.lexisoup.data.translator.aggregation.TranslatorsAggregator
import blazern.lexisoup.data.translator.api.Translator
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.feature.search_results.FakeTranslator
import blazern.lexisoup.feature.search_results.model.TranslationState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CreateTranslationsStatesUseCaseTest {

    private val canTranslate = CanTranslateUseCase()

    @Test
    fun `returns empty list when no translator can translate any detail`() = runTest {
        val detail = exampleDetail(
            text = "some text",
            lang = Lang.EN,
        )

        val source1 = DataSource.Backend(impl = null)
        val source2 = DataSource.Kaikki

        val translators = fakeAggregator(
            translatorsBySource = mapOf(
                source1 to translatorWithCapabilities(
                    source = source1,
                    capabilities = Translator.Capabilities(
                        langs = mapOf(Lang.EN to setOf(Lang.FR)), // does NOT include DE
                        textLengthMax = 100,
                        textLengthMin = 0,
                        translateBatchSizeLimit = 10,
                    )
                ),
                source2 to translatorWithCapabilities(
                    source = source2,
                    capabilities = Translator.Capabilities(
                        langs = mapOf(Lang.DE to setOf(Lang.EN)), // wrong direction for EN->DE
                        textLengthMax = 100,
                        textLengthMin = 0,
                        translateBatchSizeLimit = 10,
                    )
                ),
            ),
            dataSourcesOrder = listOf(source1, source2),
        )

        val useCase = CreateTranslationsStatesUseCaseImpl(
            translators = translators,
            canTranslate = canTranslate,
        )

        val result = useCase(
            details = listOf(detail),
            langFrom = Lang.EN,
            langTo = Lang.DE,
        )

        assertEquals(emptyList(), result)
    }

    @Test
    fun `adds CanStart for each source that can translate at least one detail`() = runTest {
        val details = listOf(
            exampleDetail("some text", Lang.EN),
            exampleDetail("some other text", Lang.EN),
        )

        val s1 = DataSource.Backend(impl = null)
        val s2 = DataSource.Kaikki
        val s3 = DataSource.Tatoeba

        val translators = fakeAggregator(
            translatorsBySource = mapOf(
                s1 to translatorWithCapabilities(
                    source = s1,
                    capabilities = Translator.Capabilities(
                        langs = mapOf(Lang.EN to setOf(Lang.DE)), // can
                        textLengthMax = 100,
                        textLengthMin = 0,
                        translateBatchSizeLimit = 10,
                    )
                ),
                s2 to translatorWithCapabilities(
                    source = s2,
                    capabilities = Translator.Capabilities(
                        langs = mapOf(Lang.EN to setOf(Lang.FR)), // cannot
                        textLengthMax = 100,
                        textLengthMin = 0,
                        translateBatchSizeLimit = 10,
                    )
                ),
                s3 to translatorWithCapabilities(
                    source = s3,
                    capabilities = Translator.Capabilities(
                        langs = mapOf(Lang.EN to setOf(Lang.DE)), // can
                        textLengthMax = 100,
                        textLengthMin = 0,
                        translateBatchSizeLimit = 10,
                    )
                ),
            ),
            dataSourcesOrder = listOf(s1, s2, s3),
        )

        val useCase = CreateTranslationsStatesUseCaseImpl(
            translators = translators,
            canTranslate = canTranslate,
        )

        val result = useCase(
            details = details,
            langFrom = Lang.EN,
            langTo = Lang.DE,
        )

        val expected = listOf(
            TranslationState.CanStart(s1),
            TranslationState.CanStart(s3),
        )

        assertEquals(expected, result)
    }

    private fun exampleDetail(text: String, lang: Lang): LexicalItemDetail.Example =
        LexicalItemDetail.Example(
            TranslationsSet(
                original = Sentence(text, lang, DataSource.Kaikki),
                translations = emptyList(),
                translationsQualities = emptyList(),
            ),
            DataSource.Kaikki,
        )

    private fun translatorWithCapabilities(
        source: DataSource,
        capabilities: Translator.Capabilities,
    ): FakeTranslator = FakeTranslator(
        source = source,
        capabilities = flowOf(capabilities),
    )

    private fun fakeAggregator(
        translatorsBySource: Map<DataSource, Translator>,
        dataSourcesOrder: List<DataSource>,
    ): TranslatorsAggregator =
        object : TranslatorsAggregator {
            override val dataSources: List<DataSource> = dataSourcesOrder

            override fun getTranslator(source: DataSource): Translator =
                translatorsBySource.getValue(source)
        }
}
