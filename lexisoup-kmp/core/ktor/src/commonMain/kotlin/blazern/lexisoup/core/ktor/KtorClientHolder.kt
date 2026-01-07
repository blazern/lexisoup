package blazern.lexisoup.core.ktor

import blazern.lexisoup.core.logging.Log
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class KtorClientHolder(engine: HttpClientEngine? = null) {
    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    val client = createHttpClient(engine) {
        install(ContentNegotiation) {
            json(jsonConfig)
            json(
                json = jsonConfig,
                contentType = ContentType.Application.OctetStream
            )
        }
        HttpResponseValidator {
            handleResponseExceptionWithRequest { exception, request ->
                Log.e("KtorClientHolder", exception) { "Ktor error for ${request.url}" }
            }
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.i("KtorClientHolder") { message }
                }
            }
            level = LogLevel.ALL
        }
    }
}

private fun createHttpClient(
    engine: HttpClientEngine?,
    block: HttpClientConfig<*>.() -> Unit,
): HttpClient {
    return if (engine != null) {
        HttpClient(engine, block)
    } else {
        HttpClient(block)
    }
}
