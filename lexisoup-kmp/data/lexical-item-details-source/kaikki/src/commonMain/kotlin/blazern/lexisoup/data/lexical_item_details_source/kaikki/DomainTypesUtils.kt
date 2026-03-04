package blazern.lexisoup.data.lexical_item_details_source.kaikki

import blazern.lexisoup.core.logging.Log
import blazern.lexisoup.data.kaikki.model.Entry
import blazern.lexisoup.data.kaikki.model.Form
import blazern.lexisoup.domain.model.Article
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.LexicalItemDetail.Explanation
import blazern.lexisoup.domain.model.PartOfSpeech
import blazern.lexisoup.domain.model.Pronoun
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_MAX
import blazern.lexisoup.domain.model.WordForm
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.MainForm
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
internal fun Entry.toDetails(
    langFrom: Lang,
    langTo: Lang,
): List<LexicalItemDetail> {
    val src = DataSource.Kaikki
    val result = mutableListOf<LexicalItemDetail>()

    val pos = pos.stringToPos()
    val tags = tags.map { it.toWordFormTag() } + MainForm()
    val mainWordForm = WordForm(
        text = word
            .split(wiktionaryDelimiter)
            .joinToString(" ") { it.onlyLetters() },
        textRaw = word,
        wordsCount = word.split(wiktionaryDelimiter).size,
        tags = tags,
        lang = langFrom,
        articles = extractArticle(pos, tags, langFrom)?.let { setOf(it) } ?: emptySet()
    )
    result += LexicalItemDetail.Forms(
        LexicalItemDetail.Forms.Value.Detailed(
            forms.map { it.toDomain(langFrom, pos) } + mainWordForm
        ),
        langFrom,
        src,
        pos = pos,
    )
    if (senses.isNotEmpty()) {
        for (sense in senses) {
            for (gloss in sense.glosses) {
                result += Explanation(
                    Sentence(gloss, langFrom, src),
                    src,
                )
            }
            for (example in sense.examples) {
                result += LexicalItemDetail.Example(
                    TranslationsSet(
                        original = Sentence(example.text, langFrom, src),
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
            words.map { Sentence(it, langFrom, src) },
            src,
        )
    }
    val etymologies = buildList {
        etymologyText?.let { add(it) }
        addAll(etymologyTexts)
    }
    etymologies.forEach {
        result += LexicalItemDetail.Etymology(
            translationsSet = TranslationsSet(
                original = Sentence(it, langFrom, src),
                translations = emptyList(),
                translationsQualities = emptyList(),
            ),
            src,
        )
    }
    return result
}

private fun extractArticle(pos: PartOfSpeech, tags: List<WordForm.Tag>, lang: Lang): Article? {
    if (pos !is PartOfSpeech.Noun) {
        return null
    }
    when (lang) {
        Lang.DE -> {}
        Lang.FR -> {}
        Lang.RU -> return null
        Lang.EN -> return null
    }
    tags.forEach {
        when (it) {
            is WordForm.Tag.Defined.Masculine -> {
                return when (lang) {
                    Lang.DE -> Article.allOf(lang)["der"]
                    Lang.FR -> Article.allOf(lang)["le"]
                }
            }
            is WordForm.Tag.Defined.Feminine -> {
                return when (lang) {
                    Lang.DE -> Article.allOf(lang)["die"]
                    Lang.FR -> Article.allOf(lang)["la"]
                }
            }
            is WordForm.Tag.Defined.Neuter -> {
                return when (lang) {
                    Lang.DE -> Article.allOf(lang)["das"]
                    else -> {
                        Log.w(TAG) { "Neuter article for $lang not supported" }
                        null
                    }
                }
            }
            else -> {}
        }
    }
    return null
}

private fun Form.toDomain(lang: Lang, pos: PartOfSpeech): WordForm {
    val textRaw = form.trim()
    val parts = textRaw.split(wiktionaryDelimiter)
    val articles = when (pos) {
        is PartOfSpeech.Noun -> {
            val articleParts = if (article != null) {
                parts + article
            } else {
                parts
            }
            Article.allOf(lang).filter { articleParts.contains(it.key) }
        }
        else -> emptyMap()
    }

    val pronouns = when (pos) {
        is PartOfSpeech.Verb -> {
            Pronoun.allOf(lang).filter { parts.contains(it.key) }
        }
        else -> emptyMap()
    }
    val partsCleaned = parts.filter { !articles.contains(it) && !pronouns.contains(it) }
    val text = partsCleaned.joinToString(" ") { it.onlyLetters() }

    return WordForm(
        text = text,
        textRaw = textRaw,
        wordsCount = partsCleaned.size,
        tags = (tags ?: emptyList()).map { it.toWordFormTag() },
        lang = lang,
        articles = articles.values.toSet(),
        pronouns = pronouns.values.toSet(),
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
        "participle-i" -> WordForm.Tag.Defined.Participle(this)
        "participle-2" -> WordForm.Tag.Defined.Participle2(this)
        "participle-ii" -> WordForm.Tag.Defined.Participle2(this)
        "present" -> WordForm.Tag.Defined.Present(this)
        "past" -> WordForm.Tag.Defined.Past(this)
        "indicative" -> WordForm.Tag.Defined.Indicative(this)
        "subjunctive-i" -> WordForm.Tag.Defined.SubjunctiveI(this)
        "subjunctive-ii" -> WordForm.Tag.Defined.SubjunctiveII(this)
        "imperative" -> WordForm.Tag.Defined.Imperative(this)
        "preterite" -> WordForm.Tag.Defined.Preterite(this)
        "perfect" -> WordForm.Tag.Defined.Perfect(this)
        "pluperfect" -> WordForm.Tag.Defined.Pluperfect(this)
        "future" -> WordForm.Tag.Defined.Future(this)
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
        "masculine" -> WordForm.Tag.Defined.Masculine(this)
        "feminine" -> WordForm.Tag.Defined.Feminine(this)
        "neuter" -> WordForm.Tag.Defined.Neuter(this)
        else -> WordForm.Tag.Undefined(this)
    }
}

private fun String.stringToPos(): PartOfSpeech {
    return when (this) {
        "noun" -> PartOfSpeech.Noun
        "verb" -> PartOfSpeech.Verb
        else -> PartOfSpeech.Other(this)
    }
}

private fun String.onlyLetters(): String =
    replace(Regex("[^\\p{L}]"), "")

private val wiktionaryDelimiter = Regex("""[\\/ ]""")

private const val TAG = "DomainTypesUtilsTest"