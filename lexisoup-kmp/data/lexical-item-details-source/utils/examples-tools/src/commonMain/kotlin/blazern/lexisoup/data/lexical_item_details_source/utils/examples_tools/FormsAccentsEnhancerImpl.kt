package blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools

import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TextAccent
import blazern.lexisoup.domain.model.WordForm

class FormsAccentsEnhancerImpl internal constructor(
    private val forms: List<WordForm>,
) : FormsAccentsEnhancer {
    override fun enhance(
        sentence: Sentence,
    ): Sentence {
        val forms = forms
            .map { it.text }
            .filter { it.isNotEmpty() }

        val text = sentence.text
        val accents = mutableSetOf<TextAccent>()
        for (form in forms) {
            var startIndex = 0
            while (true) {
                startIndex = text.indexOf(form, startIndex, ignoreCase = true)
                if (startIndex == -1) {
                    break
                }
                accents.add(TextAccent(startIndex, startIndex + form.length))
                startIndex = startIndex + form.length
            }
        }

        return sentence.copy(
            textAccents = sentence.textAccents + accents,
        )
    }
}
