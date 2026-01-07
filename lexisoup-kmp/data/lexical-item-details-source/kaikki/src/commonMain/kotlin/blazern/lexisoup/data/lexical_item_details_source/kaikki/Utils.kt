package blazern.lexisoup.data.lexical_item_details_source.kaikki

import blazern.lexisoup.data.kaikki.model.Entry
import blazern.lexisoup.data.kaikki.model.Form
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.LexicalItemDetail.Explanation
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_MAX
import blazern.lexisoup.domain.model.WordForm
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
internal fun Entry.toDetails(
    langFrom: Lang,
    langTo: Lang,
): List<LexicalItemDetail> {
    val src = DataSource.KAIKKI
    val result = mutableListOf<LexicalItemDetail>()

    if (forms.isNotEmpty()) {
        result += LexicalItemDetail.Forms(
            LexicalItemDetail.Forms.Value.Detailed(
                forms.map { it.toDomain(langFrom) }
            ),
            src,
        )
    }
    if (senses.isNotEmpty()) {
        for (sense in senses) {
            for (gloss in sense.glosses) {
                result += Explanation(gloss, src)
            }
            for (example in sense.examples) {
                result += LexicalItemDetail.Example(
                    TranslationsSet(
                        original = Sentence(example.text, langTo, src),
                        translations = emptyList(),
                        translationsQualities = emptyList(),
                    ),
                    src,
                )
            }
        }
    }
    val translations = translations.filter { it.langCode == langTo.iso2 }
    if (translations.isNotEmpty()) {
        val words = translations.mapNotNull { it.word }
        result += LexicalItemDetail.WordTranslations(
            TranslationsSet(
                original = Sentence(word, langFrom, src),
                translations = words.map { Sentence(it, langTo, src) },
                translationsQualities = words.map { QUALITY_MAX },
            ),
            src,
        )
    }
    val synonyms = synonyms + coordinateTerms
    if (synonyms.isNotEmpty()) {
        val words = synonyms.mapNotNull { it.word }
        result += LexicalItemDetail.Synonyms(
            TranslationsSet(
                original = Sentence(word, langFrom, src),
                translations = words.map { Sentence(it, langFrom, src) },
                translationsQualities = words.map { QUALITY_MAX },
            ),
            src,
        )
    }
    return result
}

private fun Form.toDomain(lang: Lang): WordForm {
    return WordForm(
        text = form.trim(),
        tags = (tags ?: emptyList()).map { it.toWordFormTag() },
        lang = lang,
    )
}

private fun String.toWordFormTag(): WordForm.Tag {
    return when (this.lowercase()) {
        "nominative" -> WordForm.Tag.Defined.Nominative(this)
        "accusative" -> WordForm.Tag.Defined.Accusative(this)
        "dative" -> WordForm.Tag.Defined.Dative(this)
        "genitive" -> WordForm.Tag.Defined.Genitive(this)
        "active" -> WordForm.Tag.Defined.Active(this)
        "infinitive" -> WordForm.Tag.Defined.Infinitive(this)
        "participle" -> WordForm.Tag.Defined.Participle(this)
        "present" -> WordForm.Tag.Defined.Present(this)
        "past" -> WordForm.Tag.Defined.Past(this)
        "indicative" -> WordForm.Tag.Defined.Indicative(this)
        "subjunctive-i" -> WordForm.Tag.Defined.SubjunctiveI(this)
        "subjunctive-ii" -> WordForm.Tag.Defined.SubjunctiveII(this)
        "imperative" -> WordForm.Tag.Defined.Imperative(this)
        "preterite" -> WordForm.Tag.Defined.Preterite(this)
        "perfect" -> WordForm.Tag.Defined.Perfect(this)
        "pluperfect" -> WordForm.Tag.Defined.Pluperfect(this)
        "future-i" -> WordForm.Tag.Defined.FutureI(this)
        "future-ii" -> WordForm.Tag.Defined.FutureII(this)
        "first-person" -> WordForm.Tag.Defined.FirstPerson(this)
        "second-person" -> WordForm.Tag.Defined.SecondPerson(this)
        "third-person" -> WordForm.Tag.Defined.ThirdPerson(this)
        "singular" -> WordForm.Tag.Defined.Singular(this)
        "plural" -> WordForm.Tag.Defined.Plural(this)
        "informal" -> WordForm.Tag.Defined.Informal(this)
        "formal" -> WordForm.Tag.Defined.Formal(this)
        "rare" -> WordForm.Tag.Defined.Rare(this)
        "auxiliary" -> WordForm.Tag.Defined.Auxiliary(this)
        "multiword-construction" -> WordForm.Tag.Defined.MultiwordConstruction(this)
        else -> WordForm.Tag.Undefined(this)
    }
}
