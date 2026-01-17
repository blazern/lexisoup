package blazern.lexisoup.domain.model

import blazern.lexisoup.domain.model.Pronoun.Reading
import blazern.lexisoup.domain.model.Pronoun.Tag.Gender.Feminine
import blazern.lexisoup.domain.model.Pronoun.Tag.Gender.Masculine
import blazern.lexisoup.domain.model.Pronoun.Tag.Gender.Neuter
import blazern.lexisoup.domain.model.Pronoun.Tag.Number.Plural
import blazern.lexisoup.domain.model.Pronoun.Tag.Number.Singular
import blazern.lexisoup.domain.model.Pronoun.Tag.Person.First
import blazern.lexisoup.domain.model.Pronoun.Tag.Person.Second
import blazern.lexisoup.domain.model.Pronoun.Tag.Person.Third

data class Pronoun(
    val text: String,
    val readings: Set<Reading>,
) {
    constructor(text: String, vararg readings: Reading) : this(text, readings.toSet())

    companion object {
        fun allOf(lang: Lang): Map<String, Pronoun> = when (lang) {
            Lang.EN -> PronounsEnglish
            Lang.DE -> PronounsGerman
            Lang.RU -> PronounsRussian
            Lang.FR -> PronounsFrench
        }
    }

    data class Reading(val tags: Set<Tag>) {
        constructor(vararg tags: Tag) : this(tags.toSet())
    }

    sealed interface Tag {
        sealed interface Person : Tag {
            object First : Person  // I, we
            object Second : Person // you
            object Third : Person  // he, she, they
        }
        sealed interface Number : Tag {
            object Singular : Number // I
            object Plural : Number   // we
        }
        sealed interface Gender : Tag {
            object Masculine : Gender // he
            object Feminine : Gender  // she
            object Neuter : Gender    // it
        }
    }
}

private val PronounsEnglish = setOf(
    Pronoun("I", Reading(First, Singular)),
    Pronoun("you", Reading(Second, Singular), Reading(Second, Plural)),
    Pronoun("he", Reading(Third, Singular, Masculine)),
    Pronoun("she", Reading(Third, Singular, Feminine)),
    Pronoun("it", Reading(Third, Singular, Neuter)),
    Pronoun("we", Reading(First, Plural)),
    Pronoun("they", Reading(Third, Plural)),
).associateBy { it.text }

private val PronounsGerman = setOf(
    Pronoun("Sie", Reading(Second, Singular), Reading(Second, Plural)),
    Pronoun("ich", Reading(First, Singular)),
    Pronoun("du", Reading(Second, Singular)),
    Pronoun("er", Reading(Third, Singular, Masculine)),
    Pronoun("sie", Reading(Third, Singular, Feminine), Reading(Third, Plural)),
    Pronoun("es", Reading(Third, Singular, Neuter)),
    Pronoun("wir", Reading(First, Plural)),
    Pronoun("ihr", Reading(Second, Plural), Reading(Third, Singular, Feminine)),
).associateBy { it.text }

private val PronounsRussian = setOf(
    Pronoun("я", Reading(First, Singular)),
    Pronoun("ты", Reading(Second, Singular)),
    Pronoun("он", Reading(Third, Singular, Masculine)),
    Pronoun("она", Reading(Third, Singular, Feminine)),
    Pronoun("оно", Reading(Third, Singular, Neuter)),
    Pronoun("мы", Reading(First, Plural)),
    Pronoun("Вы", Reading(Second, Singular), Reading(Second, Plural)),
    Pronoun("вы", Reading(Second, Plural)),
    Pronoun("они", Reading(Third, Plural)),
).associateBy { it.text }

private val PronounsFrench = setOf(
    Pronoun("je", Reading(First, Singular)),
    Pronoun("tu", Reading(Second, Singular)),
    Pronoun("il", Reading(Third, Singular, Masculine)),
    Pronoun("elle", Reading(Third, Singular, Feminine)),
    Pronoun("on", Reading(Third, Singular)),
    Pronoun("nous", Reading(First, Plural)),
    Pronoun("vous", Reading(Second, Singular), Reading(Second, Plural)),
    Pronoun("ils", Reading(Third, Plural, Masculine)),
    Pronoun("elles", Reading(Third, Plural, Feminine)),
).associateBy { it.text }
