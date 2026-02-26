package blazern.lexisoup.feature.search_results

import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.feature.search_results.usecases.TransformPageUseCase

internal class FakeTransformPageUseCase(
    private val impl: (page: Item.Page) -> List<Item.Page>,
) : TransformPageUseCase() {
    override operator fun invoke(page: Item.Page) = impl(page)
}
