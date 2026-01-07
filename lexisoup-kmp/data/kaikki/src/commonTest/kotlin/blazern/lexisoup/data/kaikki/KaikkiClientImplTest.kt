package blazern.lexisoup.data.kaikki

import arrow.core.Either
import arrow.core.getOrElse
import blazern.lexisoup.core.ktor.KtorClientHolder
import blazern.lexisoup.data.kaikki.model.Entry
import blazern.lexisoup.domain.backend_address.BackendAddressProvider
import blazern.lexisoup.domain.model.Lang
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KaikkiClientImplTest {
    private lateinit var response: ()->Either<Exception, Pair<HttpStatusCode, String>>

    private val mockEngine = MockEngine {
        response().fold(
            { throw it },
            { (code, body) ->
                respond(
                    content = ByteReadChannel(body),
                    status = code,
                    headers = headersOf(HttpHeaders.ContentType, "application/octet-stream")
                )
            }
        )
    }

    private val clientHolder = KtorClientHolder(mockEngine)
    private val kaikki = KaikkiClientImpl(clientHolder, FakeBackendAddressProvider())

    @Test
    fun `search returns list of entries`() = runTest {
        setResponse(
            """
            {"word":"Haus","pos":"noun","pos_title":"Substantiv","lang_code":"de","lang":"German"}
            {"word":"Haus","pos":"verb","pos_title":"Verb","lang_code":"de","lang":"German"}
            """.trimIndent()
        )

        val result = kaikki.search("Haus", Lang.DE).getOrElse { throw it.e!! }

        val expected = listOf(
            Entry(
                word = "Haus",
                pos = "noun",
                posTitle = "Substantiv",
                langCode = "de",
                lang = "German"
            ),
            Entry(
                word = "Haus",
                pos = "verb",
                posTitle = "Verb",
                langCode = "de",
                lang = "German"
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun `empty body yields empty list`() = runTest {
        setResponse("")
        val result = kaikki.search("Haus", Lang.DE).getOrElse { throw it.e!! }
        assertTrue(result.isEmpty())
    }

    @Test
    fun `network exception is wrapped in Either Left`() = runTest {
        val io = IOException("no internet")
        setResponse(io)
        val result = kaikki.search("Haus", Lang.DE)
        assertEquals(io, result.leftOrNull()?.e!!.cause)
    }

    @Test
    fun `serialization problems are wrapped in Either Left`() = runTest {
        setResponse("{ this is not valid json }")
        val result = kaikki.search("Haus", Lang.DE)
        val error = result.leftOrNull()!!.e
        assertTrue(error is SerializationException)
    }

    @Test
    fun `search retries with lowercase if capitalized query not found`() = runTest {
        val secondResponse = """
            {"word":"haus","pos":"noun","pos_title":"Substantiv","lang_code":"de","lang":"German"}
        """.trimIndent()

        var callCount = 0
        setResponse {
            callCount++
            if (callCount == 1) {
                Either.Right(Pair(HttpStatusCode.NotFound, ""))
            } else {
                Either.Right(Pair(HttpStatusCode.OK, secondResponse))
            }
        }

        val result = kaikki.search("Haus", Lang.DE).getOrElse { throw it.e!! }
        assertEquals(1, result.size)
        assertEquals("haus", result[0].word)
    }

    @Test
    fun `search retries with uppercase if lowercase query not found`() = runTest {
        val secondResponse = """
            {"word":"Haus","pos":"noun","pos_title":"Substantiv","lang_code":"de","lang":"German"}
        """.trimIndent()

        var callCount = 0
        setResponse {
            callCount++
            if (callCount == 1) {
                Either.Right(Pair(HttpStatusCode.NotFound, ""))
            } else {
                Either.Right(Pair(HttpStatusCode.OK, secondResponse))
            }
        }

        val result = kaikki.search("haus", Lang.DE).getOrElse { throw it.e!! }
        assertEquals(1, result.size)
        assertEquals("Haus", result[0].word)
    }

    @Test
    fun `search does not retry if first response is 200 OK`() = runTest {
        val body = """
            {"word":"Haus","pos":"noun","pos_title":"Substantiv","lang_code":"de","lang":"German"}
        """.trimIndent()

        var callCount = 0
        setResponse {
            callCount++
            Either.Right(Pair(HttpStatusCode.OK, body))
        }

        val result = kaikki.search("Haus", Lang.DE).getOrElse { throw it.e!! }

        assertEquals(1, result.size)
        assertEquals("Haus", result[0].word)
        assertEquals(1, callCount)
    }

    @Test
    fun `search does not retry a third time if both responses are 404`() = runTest {
        var callCount = 0
        setResponse {
            callCount++
            Either.Right(Pair(HttpStatusCode.NotFound, "Page Not Found"))
        }

        val result = kaikki.search("unfindable", Lang.DE)
        assertTrue(result.getOrNull()?.isEmpty() == true)
        assertEquals(2, callCount)
    }

    private fun setResponse(body: String) {
        response = { Either.Right(Pair(HttpStatusCode.OK, body)) }
    }

    private fun setResponse(fn: ()->Either<Exception, Pair<HttpStatusCode, String>>) {
        response = fn
    }

    private fun setResponse(exception: Exception) {
        response = { Either.Left(exception) }
    }
}

private class FakeBackendAddressProvider(
    override val baseUrl: Flow<String> = flowOf("my.webserver.com"),
    override val isLocalhost: Flow<Boolean> = flowOf(false),
) : BackendAddressProvider {
    override suspend fun setIsLocalhost(isLocalhost: Boolean) = throw NotImplementedError()
}
