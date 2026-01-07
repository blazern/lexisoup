package blazern.lexisoup.data.lexical_item_details_source.tatoeba

import arrow.core.getOrElse
import blazern.lexisoup.core.ktor.KtorClientHolder
import blazern.lexisoup.core.logging.Log
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.FormsForExamplesProvider
import blazern.lexisoup.domain.backend_address.BackendAddressProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TatoebaLexicalItemDetailsSource internal constructor(
    private val tatoebaClient: TatoebaClient,
    private val formsForExamplesProvider: FormsForExamplesProvider,
) : LexicalItemDetailsSource {

    constructor(
        ktorClientHolder: KtorClientHolder,
        backendAddressProvider: BackendAddressProvider,
        formsForExamplesProvider: FormsForExamplesProvider,
    ) : this(
        tatoebaClient = TatoebaClientImpl(ktorClientHolder, backendAddressProvider),
        formsForExamplesProvider = formsForExamplesProvider
    )

    override val source = DataSource.TATOEBA
    override val types = setOf(LexicalItemDetail.Type.EXAMPLE)

    override fun request(
        query: String,
        langFrom: Lang,
        langTo: Lang
    ): Flow<Item> = requestImpl(query, langFrom, langTo)

    private fun requestImpl(
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ): Flow<Item>  = flow {
        val queriesRes = formsForExamplesProvider.requestFor(
            query = query,
            langFrom = langFrom,
            langTo = langTo,
        )
        val finalQuery = queriesRes.map {
            it.joinToString("|", "(", ")") { "=${it.text}" }
        }.onLeft {
            Log.e(TAG, it.e) { "No forms" }
        }

        var hasNextPage = true
        var page = 1
        while (hasNextPage) {
            val translationsSetsResult = tatoebaClient.search(
                query = finalQuery.getOrElse { query },
                langFrom = langFrom,
                langTo = langTo,
                page = page,
            )

            val translationsSets = translationsSetsResult.fold(
                { emit(Item.Failure(it)); continue },
                { it }
            )
            val result = Item.Page(
                details = translationsSets.map {
                    LexicalItemDetail.Example(
                        translationsSet = it,
                        source = source,
                    )
                },
                errors = finalQuery.fold({ listOf(it) }, { emptyList() }),
                nextPageTypes = types,
            )
            emit(result)
            page += 1
            hasNextPage = translationsSets.isNotEmpty()
        }
    }
}

private const val TAG = "TatoebaLexicalItemDetailsSource"
