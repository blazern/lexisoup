package blazern.lexisoup.feature.search_results.ui.list.model

import blazern.lexisoup.domain.model.Gender
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.LexicalItemDetail.Forms
import blazern.lexisoup.domain.model.PartOfSpeech
import blazern.lexisoup.domain.model.WordForm
import blazern.lexisoup.feature.search_results.forms.extractGenderFrom
import blazern.lexisoup.feature.search_results.forms.selectNounFormsForHeader
import blazern.lexisoup.feature.search_results.forms.selectVerbFormsForHeader

internal data class SelectedHeader(
    val title: String,
    val sourceDetail: LexicalItemDetail,
    val detailConsumed: Boolean,
    val pos: PartOfSpeech? = null,
) {
    companion object
}

internal fun SelectedHeader.Companion.select(details: List<LexicalItemDetail>): SelectedHeader? {
    val forms = details.filterIsInstance<Forms>().firstOrNull()
    if (forms != null) {
        val value = forms.value
        return when (value) {
            is Forms.Value.Text -> SelectedHeader(
                title = value.text,
                pos = forms.pos,
                sourceDetail = forms,
                detailConsumed = true,
            )
            is Forms.Value.Detailed -> SelectedHeader.createFor(value, forms.lang, forms)
        }
    }
    return null
}

private fun SelectedHeader.Companion.createFor(
    detailedForms: Forms.Value.Detailed,
    lang: Lang,
    source: Forms,
): SelectedHeader? {
    if (detailedForms.forms.isEmpty()) {
        return null
    }
    return when (source.pos) {
        PartOfSpeech.Noun -> SelectedHeader.forNoun(detailedForms.forms, lang, source)
        PartOfSpeech.Verb -> SelectedHeader.forVerb(detailedForms.forms, source)
        null -> SelectedHeader.forMostImportantOf(detailedForms.forms, source)
        else -> SelectedHeader.forMostImportantOf(detailedForms.forms, source)
    }
}

private fun SelectedHeader.Companion.forNoun(
    forms: List<WordForm>,
    lang: Lang,
    source: Forms,
): SelectedHeader {
    val importantForms = selectNounFormsForHeader(forms, source.lang)
    val text = importantForms.joinToString(", ") {
        val prefix = it.articles
            .takeIf { it.isNotEmpty() }
            ?.joinToString(postfix = " ") { it.text }
            .orEmpty()
        prefix + it.text
    }
    val langSpecificPrefix = when (lang) {
        Lang.RU -> {
            val gender = extractGenderFrom(forms)
            // NOTE: we don't do i18n for the following strings,
            // because they must be always displayed in their language
            when (gender) {
                Gender.MASCULINE -> "(м) "
                Gender.FEMININE -> "(ж) "
                Gender.NEUTER -> "(с) "
                null -> ""
            }
        }
        Lang.EN -> ""
        Lang.DE -> ""
        Lang.FR -> ""
    }
    return SelectedHeader(
        title = langSpecificPrefix + text,
        pos = source.pos,
        sourceDetail = source,
        detailConsumed = false,
    )
}

private fun SelectedHeader.Companion.forVerb(
    forms: List<WordForm>,
    source: Forms,
): SelectedHeader {
    val importantForms = selectVerbFormsForHeader(forms, source.lang)
    val text = importantForms.joinToString(", ") { it.text }
    return SelectedHeader(
        title = text,
        pos = source.pos,
        sourceDetail = source,
        detailConsumed = false,
    )
}

private fun SelectedHeader.Companion.forMostImportantOf(
    forms: List<WordForm>,
    source: Forms,
) = SelectedHeader(
    title = forms
        .sortedBy { it.importance }
        .asReversed()
        .first()
        .text,
    pos = source.pos,
    sourceDetail = source,
    detailConsumed = false,
)
