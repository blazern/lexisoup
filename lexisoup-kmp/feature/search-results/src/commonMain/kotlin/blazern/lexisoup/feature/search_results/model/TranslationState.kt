package blazern.lexisoup.feature.search_results.model

import blazern.lexisoup.domain.model.DataSource

sealed interface TranslationState {
    val translationSource: DataSource

    data class CanStart(override val translationSource: DataSource) : TranslationState
    data class InProgress(override val translationSource: DataSource) : TranslationState
}
