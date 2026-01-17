package blazern.lexisoup.domain.model

data class WordForm(
    val text: String,
    val lang: Lang,
    val tags: List<Tag> = emptyList(),
    val wordsCount: Int = 1,
    val articles: Set<Article> = emptySet(),
    val pronouns: Set<Pronoun> = emptySet(),
    val textRaw: String = text,
) {
    val hasArticle = articles.isNotEmpty()
    val hasPronoun = pronouns.isNotEmpty()
    val auxiliary: Boolean
        get() = tags.any { it is Tag.Defined.Auxiliary }
    val importance: Int
        get() = calculateImportanceOf(this)

    sealed interface Tag {
        val value: String

        data class Undefined(
            override val value: String,
        ) : Tag

        sealed interface Defined : Tag {
            data class MainForm(override val value: String) : Defined
            data class Nominative(override val value: String) : Defined
            data class Accusative(override val value: String) : Defined
            data class Dative(override val value: String) : Defined
            data class Genitive(override val value: String) : Defined
            data class Active(override val value: String) : Defined
            data class Infinitive(override val value: String) : Defined
            data class Participle(override val value: String) : Defined
            data class Participle2(override val value: String) : Defined
            data class Present(override val value: String) : Defined
            data class Past(override val value: String) : Defined
            data class Indicative(override val value: String) : Defined
            data class SubjunctiveI(override val value: String) : Defined
            data class SubjunctiveII(override val value: String) : Defined
            data class Imperative(override val value: String) : Defined
            data class Preterite(override val value: String) : Defined
            data class Perfect(override val value: String) : Defined
            data class Pluperfect(override val value: String) : Defined
            data class Future(override val value: String) : Defined
            data class FutureI(override val value: String) : Defined
            data class FutureII(override val value: String) : Defined
            data class FirstPerson(override val value: String) : Defined
            data class SecondPerson(override val value: String) : Defined
            data class ThirdPerson(override val value: String) : Defined
            data class Singular(override val value: String) : Defined
            data class Plural(override val value: String) : Defined
            data class Informal(override val value: String) : Defined
            data class Formal(override val value: String) : Defined
            data class Rare(override val value: String) : Defined
            data class MultiwordConstruction(override val value: String) : Defined
            data class Auxiliary(override val value: String) : Defined
            data class Masculine(override val value: String) : Defined
            data class Feminine(override val value: String) : Defined
            data class Neuter(override val value: String) : Defined
        }
    }
}

@Suppress("MagicNumber")
private fun calculateImportanceOf(form: WordForm): Int {
    return form.tags.sumOf {
        when (it) {
            is WordForm.Tag.Defined.MainForm -> 100
            // Verbs
            is WordForm.Tag.Defined.Infinitive -> 1
            is WordForm.Tag.Defined.Present -> 1
            is WordForm.Tag.Defined.Active -> 1
            // Nouns
            is WordForm.Tag.Defined.Singular -> 1
            is WordForm.Tag.Defined.Nominative -> 1
            else -> 0
        }
    }
}
