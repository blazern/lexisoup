package blazern.lexisoup.domain.model

import blazern.lexisoup.domain.model.Article.Reading
import blazern.lexisoup.domain.model.Article.Tag.Case.Accusative
import blazern.lexisoup.domain.model.Article.Tag.Case.Dative
import blazern.lexisoup.domain.model.Article.Tag.Case.Genitive
import blazern.lexisoup.domain.model.Article.Tag.Case.Nominative
import blazern.lexisoup.domain.model.Article.Tag.Definiteness.Definite
import blazern.lexisoup.domain.model.Article.Tag.Definiteness.Indefinite
import blazern.lexisoup.domain.model.Article.Tag.Definiteness.Partitive
import blazern.lexisoup.domain.model.Article.Tag.Gender.Feminine
import blazern.lexisoup.domain.model.Article.Tag.Gender.Masculine
import blazern.lexisoup.domain.model.Article.Tag.Gender.Neuter
import blazern.lexisoup.domain.model.Article.Tag.Number.Plural
import blazern.lexisoup.domain.model.Article.Tag.Number.Singular

data class Article(
    val text: String,
    val readings: Set<Reading>,
) {
    constructor(text: String, vararg readings: Reading) : this(text, readings.toSet())

    data class Reading(val tags: Set<Tag>) {
        constructor(vararg tags: Tag) : this(tags.toSet())
    }

    companion object {
        fun allOf(lang: Lang): Map<String, Article> = when (lang) {
            Lang.EN -> ArticlesEnglish
            Lang.DE -> ArticlesGerman
            Lang.RU -> ArticlesRussian
            Lang.FR -> ArticlesFrench
        }
    }

    sealed interface Tag {
        sealed interface Definiteness : Tag {
            object Definite : Definiteness
            object Indefinite : Definiteness
            object Partitive : Definiteness
        }

        sealed interface Number : Tag {
            object Singular : Number
            object Plural : Number
        }

        sealed interface Gender : Tag {
            object Masculine : Gender
            object Feminine : Gender
            object Neuter : Gender
        }

        sealed interface Case : Tag {
            object Nominative : Case
            object Accusative : Case
            object Dative : Case
            object Genitive : Case
        }
    }
}

private val ArticlesRussian = emptyMap<String, Article>()

private val ArticlesEnglish = setOf(
    Article("the", Reading(Definite, Singular), Reading(Definite, Plural)),
    Article("a", Reading(Indefinite, Singular)),
    Article("an", Reading(Indefinite, Singular)),
).associateBy { it.text }

private val ArticlesGerman = setOf(
    // Definite
    Article("der",
        Reading(Definite, Nominative, Singular, Masculine), // der Mann
        Reading(Definite, Genitive, Singular, Feminine),    // der Frau
        Reading(Definite, Dative, Singular, Feminine),      // mit der Frau
        Reading(Definite, Genitive, Plural),                // der Kinder
    ),
    Article("die",
        Reading(Definite, Nominative, Singular, Feminine),  // die Frau
        Reading(Definite, Accusative, Singular, Feminine),  // die Frau
        Reading(Definite, Nominative, Plural),              // die Kinder
        Reading(Definite, Accusative, Plural),              // die Kinder
    ),
    Article("das",
        Reading(Definite, Nominative, Singular, Neuter),    // das Kind
        Reading(Definite, Accusative, Singular, Neuter),    // das Kind
    ),
    Article("den",
        Reading(Definite, Accusative, Singular, Masculine), // den Mann
        Reading(Definite, Dative, Plural),                  // den Kindern
    ),
    Article("dem",
        Reading(Definite, Dative, Singular, Masculine),     // dem Mann
        Reading(Definite, Dative, Singular, Neuter),        // dem Kind
    ),
    Article("des",
        Reading(Definite, Genitive, Singular, Masculine),   // des Mannes
        Reading(Definite, Genitive, Singular, Neuter),      // des Kindes
    ),

    // Indefinite
    Article("ein",
        Reading(Indefinite, Nominative, Singular, Masculine), // ein Mann
        Reading(Indefinite, Nominative, Singular, Neuter),    // ein Kind
        Reading(Indefinite, Accusative, Singular, Neuter),    // ein Kind
    ),
    Article("eine",
        Reading(Indefinite, Nominative, Singular, Feminine),  // eine Frau
        Reading(Indefinite, Accusative, Singular, Feminine),  // eine Frau
    ),
    Article("einen",
        Reading(Indefinite, Accusative, Singular, Masculine), // einen Mann
    ),
    Article("einem",
        Reading(Indefinite, Dative, Singular, Masculine),     // einem Mann
        Reading(Indefinite, Dative, Singular, Neuter),        // einem Kind
    ),
    Article("eines",
        Reading(Indefinite, Genitive, Singular, Masculine),   // eines Mannes
        Reading(Indefinite, Genitive, Singular, Neuter),      // eines Kindes
    ),
).associateBy { it.text }

private val ArticlesFrench = setOf(
    // Definite
    Article("le", Reading(Definite, Singular, Masculine)),
    Article("la", Reading(Definite, Singular, Feminine)),
    Article("les", Reading(Definite, Plural)),

    // Elided definite (gender depends on the noun; still singular)
    Article("l'", Reading(Definite, Singular)),
    Article("l’", Reading(Definite, Singular)),

    // Indefinite
    Article("un", Reading(Indefinite, Singular, Masculine)),
    Article("une", Reading(Indefinite, Singular, Feminine)),

    // “des” is commonly the plural indefinite article (and overlaps with partitive usage)
    Article("des",
        Reading(Indefinite, Plural),
        Reading(Partitive, Plural),
    ),

    // Partitive / contractions
    Article("du", Reading(Partitive, Singular, Masculine)),
    Article("au", Reading(Definite, Singular, Masculine)),
    Article("aux", Reading(Definite, Plural)),
).associateBy { it.text }
