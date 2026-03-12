package blazern.lexisoup.data.lexical_item_details_source.tatoeba

import blazern.lexisoup.core.ktor.KtorClientHolder
import blazern.lexisoup.core.logging.Log
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.FormsForExamplesProvider
import blazern.lexisoup.domain.backend_address.BackendAddressProvider
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.utils.unique
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
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

    override val source = DataSource.Tatoeba
    override val types = setOf(LexicalItemDetail.Type.EXAMPLE)

    override fun request(
        query: String,
        langFrom: Lang,
        langTo: Lang
    ): Flow<Item> = requestImpl(query, langFrom, langTo).unique()

    private fun requestImpl(
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ): Flow<Item> = flow {
        val formsRes = formsForExamplesProvider.requestFor(
            query = query,
            langFrom = langFrom,
            langTo = langTo,
        )
        val errors = mutableListOf<Err>()
        formsRes.onLeft {
            Log.e(TAG, it.e) { "Forms error" }
            errors += it
        }

        val queries = formsRes.fold(
            {
                listOf(query)
            },
            {
                if (it.isEmpty()) {
                    listOf(query)
                } else {
                    listOf(
                        "=${it.first().text}",
                        it.takeLast(it.size - 1)
                            .map { it.text }
                            .distinct()
                            .joinToString("|", "(", ")") { "=$it" }
                    )
                }
            }
        )

        for (query in queries) {
            searchImpl(query, langFrom, langTo, errors)
        }
    }

    private suspend fun FlowCollector<Item>.searchImpl(
        query: String,
        langFrom: Lang,
        langTo: Lang,
        errors: MutableList<Err>
    ) {
        var hasNextPage = true
        var page = 1
        while (hasNextPage) {
            val translationsSetsResult = tatoebaClient.search(
                query = query,
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
                errors = errors,
                nextPageTypes = types,
            )
            emit(result)
            page += 1
            hasNextPage = translationsSets.isNotEmpty()
        }
    }
}

private const val TAG = "TatoebaLexicalItemDetailsSource"
