package blazern.lexisoup.feature.search_results.model

import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.LexicalItemDetail
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
internal sealed interface LexicalItemDetailsGroupState {
    val id: String
    val source: DataSource
    val types: Set<LexicalItemDetail.Type>

    data class Loading(
        override val id: String,
        override val types: Set<LexicalItemDetail.Type>,
        override val source: DataSource,
    ) : LexicalItemDetailsGroupState

    data class Loaded(
        override val id: String,
        val details: List<LexicalItemDetail>,
        override val types: Set<LexicalItemDetail.Type>,
        override val source: DataSource,
    ) : LexicalItemDetailsGroupState

    data class Error(
        override val id: String,
        val err: Err,
        override val types: Set<LexicalItemDetail.Type>,
        override val source: DataSource,
    ) : LexicalItemDetailsGroupState
}
