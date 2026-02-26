package blazern.lexisoup.feature.search_results.usecases

import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.LexicalItemDetail.Forms

internal open class TransformPageUseCase {
    open operator fun invoke(page: Item.Page): List<Item.Page> {
        val transformedDetails = page.details.mapNotNull { transform(it) }
        return if (transformedDetails.isNotEmpty()) {
            split(page.copy(details = transformedDetails))
        } else {
            emptyList()
        }
    }

    private fun transform(detail: LexicalItemDetail): LexicalItemDetail? {
        if (detail is Forms) {
            val value = detail.value
            if (value is Forms.Value.Detailed) {
                return if (value.forms.isNotEmpty()) {
                    detail
                } else {
                    null
                }
            }
        }
        return detail
    }

    private fun split(page: Item.Page): List<Item.Page> {
        val examples = page.details.filterIsInstance<LexicalItemDetail.Example>()
        val rest = page.details.filter { it !is LexicalItemDetail.Example }
        val result = mutableListOf<Item.Page>()
        if (rest.isNotEmpty()) {
            result += page.copy(details = rest)
        }
        // Each example is a unique page now, so that
        // they would be translated separately
        examples.forEach { example ->
            result += page.copy(details = listOf(example))
        }
        return result
    }
}
