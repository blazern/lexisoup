package blazern.lexisoup.model.lexical_item_details_source.chatgpt

import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.data.lexical_item_details_source.utils.cache.LexicalItemDetailsSourceCacher
import blazern.lexisoup.domain.model.DataSource.ChatGPT
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.LexicalItemDetail.Forms
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_BASIC
import blazern.lexisoup.graphql.model.LexicalItemsFromLLMQuery
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

class ChatGPTLexicalItemDetailsSourceTest {
    private val apollo = FakeApolloClientHolder()
    private val source  = ChatGPTLexicalItemDetailsSource(
        apollo,
        LexicalItemDetailsSourceCacher.NOOP,
    )

    private fun op(query: String = "Hund", from: String = "deu", to: String = "eng") =
        LexicalItemsFromLLMQuery(query, from, to)

    private fun parse(op: LexicalItemsFromLLMQuery, json: String):
            ApolloResponse<LexicalItemsFromLLMQuery.Data> {
        val reader: JsonReader = BufferedSourceJsonReader(Buffer().writeUtf8(json))
        return op.parseResponse(reader)
    }

    private fun successResponse(): ApolloResponse<LexicalItemsFromLLMQuery.Data> {
        val json = """
            {
              "data": {
                "llm": [
                  { "__typename": "Forms", "source": "chatgpt", "text": "der Hund, -e" },
                  {
                    "__typename": "Explanation",
                    "source": "chatgpt",
                    "text": "Der Hund ist ein Haustier."
                  },
                  {
                    "__typename": "WordTranslations",
                    "source": "chatgpt",
                    "translationsSet": {
                      "__typename": "TranslationsSet",
                      "original": {
                        "__typename": "Sentence",
                        "text": "Hund",
                        "langIso3": "deu",
                        "source": "chatgpt"
                      },
                      "translations": [
                        {
                          "__typename": "Sentence",
                          "text": "dog",
                          "langIso3": "eng",
                          "source": "chatgpt"
                        },
                        {
                          "__typename": "Sentence",
                          "text": "hound",
                          "langIso3": "eng",
                          "source": "chatgpt"
                        }
                      ]
                    }
                  },
                  {
                    "__typename": "Synonyms",
                    "source": "chatgpt",
                    "translationsSet": {
                      "__typename": "TranslationsSet",
                      "original": {
                        "__typename": "Sentence",
                        "text": "Hund",
                        "langIso3": "deu",
                        "source": "chatgpt"
                      },
                      "translations": [
                        {
                          "__typename": "Sentence",
                          "text": "Hündin",
                          "langIso3": "deu",
                          "source": "chatgpt"
                        },
                        {
                          "__typename": "Sentence",
                          "text": "Köter",
                          "langIso3": "deu",
                          "source": "chatgpt"
                        }
                      ]
                    }
                  },
                  {
                    "__typename": "Example",
                    "source": "chatgpt",
                    "translationsSet": {
                      "__typename": "TranslationsSet",
                      "original": {
                        "__typename": "Sentence",
                        "text": "Dog",
                        "langIso3": "eng",
                        "source": "chatgpt"
                      },
                      "translations": [
                        {
                          "__typename": "Sentence",
                          "text": "Hund",
                          "langIso3": "deu",
                          "source": "chatgpt"
                        }
                      ]
                    }
                  },
                  {
                    "__typename": "Example",
                    "source": "chatgpt",
                    "translationsSet": {
                      "__typename": "TranslationsSet",
                      "original": {
                        "__typename": "Sentence",
                        "text": "My dog",
                        "langIso3": "eng",
                        "source": "chatgpt"
                      },
                      "translations": [
                        {
                          "__typename": "Sentence",
                          "text": "Mein Hund",
                          "langIso3": "deu",
                          "source": "chatgpt"
                        }
                      ]
                    }
                  }
                ]
              }
            }
        """.trimIndent()
        return parse(op(), json)
    }

    private fun networkErrorResponse(): ApolloResponse<LexicalItemsFromLLMQuery.Data> {
        return ApolloResponse.Builder<LexicalItemsFromLLMQuery.Data>(op(), uuid4())
            .exception(DefaultApolloException("no network"))
            .build()
    }

    private fun graphqlErrorsResponse(): ApolloResponse<LexicalItemsFromLLMQuery.Data> {
        val json = """
            {
              "errors": [ { "message": "malformed response" } ],
              "data": null
            }
        """.trimIndent()
        return parse(op(), json)
    }

    @Test
    fun `good scenario`() = runTest {
        apollo.setResponses(successResponse())

        val details = source.request("Hund", Lang.DE, Lang.EN)
            .take(20)
            .toList()
            .map { (it as Item.Page).details }
            .flatten()

        val explanation = details
            .filterIsInstance<LexicalItemDetail.Explanation>()
            .single()
        assertEquals(
            LexicalItemDetail.Explanation("Der Hund ist ein Haustier.", ChatGPT),
            explanation,
        )

        val forms = details
            .filterIsInstance<Forms>()
            .single()
        assertEquals(
            Forms(Forms.Value.Text("der Hund, -e"), Lang.DE, ChatGPT),
            forms,
        )

        val translations = details
            .filterIsInstance<LexicalItemDetail.WordTranslations>()
            .single()
        assertEquals(
            LexicalItemDetail.WordTranslations(
                TranslationsSet(
                    Sentence("Hund", Lang.DE, ChatGPT),
                    listOf(
                        Sentence("dog", Lang.EN, ChatGPT),
                        Sentence("hound", Lang.EN, ChatGPT),
                    ),
                    listOf(QUALITY_BASIC, QUALITY_BASIC),
                ),
                ChatGPT,
            ),
            translations,
        )

        val synonyms = details
            .filterIsInstance<LexicalItemDetail.Synonyms>()
            .single()
        assertEquals(
            LexicalItemDetail.Synonyms(
                TranslationsSet(
                    Sentence("Hund", Lang.DE, ChatGPT),
                    listOf(
                        Sentence("Hündin", Lang.DE, ChatGPT),
                        Sentence("Köter", Lang.DE, ChatGPT),
                    ),
                    listOf(QUALITY_BASIC, QUALITY_BASIC),
                ),
                ChatGPT,
            ),
            synonyms,
        )

        val examples = details
            .filterIsInstance<LexicalItemDetail.Example>()
        val expectedSets = listOf(
            TranslationsSet(
                Sentence("Dog", Lang.EN, ChatGPT),
                listOf(Sentence("Hund", Lang.DE, ChatGPT)),
                listOf(QUALITY_BASIC),
            ),
            TranslationsSet(
                Sentence("My dog", Lang.EN, ChatGPT),
                listOf(Sentence("Mein Hund", Lang.DE, ChatGPT)),
                listOf(QUALITY_BASIC),
            ),
        )
        val expectedExamples = expectedSets.map {
            LexicalItemDetail.Example(it, ChatGPT)
        }

        assertEquals(expectedExamples, examples)
    }

    @Test
    fun `first IO error then good scenario`() = runTest {
        apollo.setResponses(
            networkErrorResponse(),
            successResponse(),
        )

        val flow = source.request("dog", Lang.EN, Lang.DE)
        val iter = FlowIterator(flow)
        assertTrue(iter.next() is Item.Failure)
        assertTrue(iter.next() is Item.Page)
        iter.close()
    }

    @Test
    fun `graphql errors`() = runTest {
        apollo.setResponses(
            graphqlErrorsResponse(),
        )

        val flow = source.request("dog", Lang.EN, Lang.DE)
        val iter = FlowIterator(flow)
        assertTrue(iter.next() is Item.Failure)
        iter.close()
    }
}
