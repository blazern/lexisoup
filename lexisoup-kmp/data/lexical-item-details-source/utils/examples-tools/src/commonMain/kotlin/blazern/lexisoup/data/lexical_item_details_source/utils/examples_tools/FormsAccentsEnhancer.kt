package blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools

import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TextAccent
import blazern.lexisoup.domain.model.WordForm

interface FormsAccentsEnhancer {
    fun enhance(
        sentence: Sentence,
    ): Sentence
}
