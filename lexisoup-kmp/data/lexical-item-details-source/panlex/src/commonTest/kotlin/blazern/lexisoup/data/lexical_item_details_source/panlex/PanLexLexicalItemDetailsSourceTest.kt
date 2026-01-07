package blazern.lexisoup.data.lexical_item_details_source.panlex

import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.data.lexical_item_details_source.utils.cache.LexicalItemDetailsSourceCacher
import blazern.lexisoup.domain.model.DataSource.PANLEX
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_BASIC
import blazern.lexisoup.graphql.model.LexicalItemsFromPanLexQuery
import blazern.lexisoup.test_utils.FakeApolloClientHolder
import blazern.lexisoup.utils.FlowIterator
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.json.BufferedSourceJsonReader
import com.apollographql.apollo.api.json.JsonReader
import com.apollographql.apollo.api.parseResponse
import com.apollographql.apollo.exception.DefaultApolloException
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okio.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PanLexLexicalItemDetailsSourceTest {
    private val apollo = FakeApolloClientHolder()
    private val source = PanLexLexicalItemDetailsSource(
        apollo,
        LexicalItemDetailsSourceCacher.NOOP,
    )

    private fun op(query: String = "Haus", from: String = "deu", to: String = "eng") =
        LexicalItemsFromPanLexQuery(query, from, to)

    private fun parse(op: LexicalItemsFromPanLexQuery, json: String):
            ApolloResponse<LexicalItemsFromPanLexQuery.Data> {
        val reader: JsonReader = BufferedSourceJsonReader(Buffer().writeUtf8(json))
        return op.parseResponse(reader)
    }

    private fun successResponse(): ApolloResponse<LexicalItemsFromPanLexQuery.Data> {
        val json = """
            {
              "data": {
                "panlex": [
                  {
                    "__typename": "WordTranslations",
                    "source": "panlex",
                    "translationsSet": {
                      "__typename": "TranslationsSet",
                      "original": {
                        "__typename": "Sentence",
                        "text": "Haus",
                        "langIso3": "deu",
                        "source": "panlex"
                      },
                      "translations": [
                        {
                          "__typename": "Sentence",
                          "text": "house",
                          "langIso3": "eng",
                          "source": "panlex"
                        },
                        {
                          "__typename": "Sentence",
                          "text": "building",
                          "langIso3": "eng",
                          "source": "panlex"
                        }
                      ],
                      "translationsQualities": [5]
                    }
                  },
                  {
                    "__typename": "Synonyms",
                    "source": "panlex",
                    "translationsSet": {
                      "__typename": "TranslationsSet",
                      "original": {
                        "__typename": "Sentence",
                        "text": "Haus",
                        "langIso3": "deu",
                        "source": "panlex"
                      },
                      "translations": [
                        {
                          "__typename": "Sentence",
                          "text": "Gebäude",
                          "langIso3": "deu",
                          "source": "panlex"
                        },
                        {
                          "__typename": "Sentence",
                          "text": "Bauwerk",
                          "langIso3": "deu",
                          "source": "panlex"
                        }
                      ],
                      "translationsQualities": [2, 3]
                    }
                  }
                ]
              }
            }
        """.trimIndent()
        return parse(op(), json)
    }

    private fun networkErrorResponse(): ApolloResponse<LexicalItemsFromPanLexQuery.Data> {
        return ApolloResponse.Builder<LexicalItemsFromPanLexQuery.Data>(op(), uuid4())
            .exception(DefaultApolloException("no network"))
            .build()
    }

    private fun graphqlErrorsResponse(): ApolloResponse<LexicalItemsFromPanLexQuery.Data> {
        val json = """
            {
              "errors": [ { "message": "malformed response" } ],
              "data": null
            }
        """.trimIndent()
        return parse(op(), json)
    }

    @Test
    fun `source id and supported types`() = runTest {
        assertEquals(PANLEX, source.source)
        assertEquals(
            setOf(
                LexicalItemDetail.Type.WORD_TRANSLATIONS,
                LexicalItemDetail.Type.SYNONYMS,
            ),
            source.types
        )
    }

    @Test
    fun `good scenario`() = runTest {
        apollo.setResponses(successResponse())

        val details = source.request("Haus", Lang.DE, Lang.EN)
            .take(10)
            .toList()
            .map { (it as Item.Page).details }
            .flatten()

        val translations = details.filterIsInstance<LexicalItemDetail.WordTranslations>().single()
        assertEquals(
            LexicalItemDetail.WordTranslations(
                TranslationsSet(
                    original = Sentence("Haus", Lang.DE, PANLEX),
                    translations = listOf(
                        Sentence("house", Lang.EN, PANLEX),
                        Sentence("building", Lang.EN, PANLEX),
                    ),
                    // The first quality is specified in the backend response, but the second is not
                    translationsQualities = listOf(5, QUALITY_BASIC)
                ),
                PANLEX
            ),
            translations
        )

        val synonyms = details.filterIsInstance<LexicalItemDetail.Synonyms>().single()
        assertEquals(
            LexicalItemDetail.Synonyms(
                TranslationsSet(
                    Sentence("Haus", Lang.DE, PANLEX),
                    listOf(
                        Sentence("Gebäude", Lang.DE, PANLEX),
                        Sentence("Bauwerk", Lang.DE, PANLEX),
                    ),
                    translationsQualities = listOf(2, 3)
                ),
                PANLEX
            ),
            synonyms
        )
    }

    @Test
    fun `first IO error then good scenario`() = runTest {
        apollo.setResponses(
            networkErrorResponse(),
            successResponse(),
        )

        val flow = source.request("Haus", Lang.DE, Lang.EN)
        val iter = FlowIterator(flow)
        assertTrue(iter.next() is Item.Failure) // network error
        assertTrue(iter.next() is Item.Page) // success afterwards
        iter.close()
    }

    @Test
    fun `graphql errors`() = runTest {
        apollo.setResponses(graphqlErrorsResponse())

        val flow = source.request("Haus", Lang.DE, Lang.EN)
        val iter = FlowIterator(flow)
        assertTrue(iter.next() is Item.Failure)
        iter.close()
    }
}
