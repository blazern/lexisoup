package blazern.lexisoup.feature.search_results.forms

import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.WordForm
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.FirstPerson
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Indicative
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Infinitive
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.MainForm
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Masculine
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Participle
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Past
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Perfect
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Present
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Singular
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.ThirdPerson
import kotlin.reflect.KClass

internal fun selectVerbFormsForHeader(
    forms: List<WordForm>,
    lang: Lang,
): List<WordForm> {
    return when (lang) {
        Lang.RU -> listOfNotNull(
            // делать
            forms.with(Infinitive::class).firstOrNull() ?: forms.with(MainForm::class).firstOrNull(),
            // делает / сделает
            forms.with(Present::class, ThirdPerson::class, Singular::class).firstOrNull(),
            // делаю / сделаю
            forms.with(Present::class, FirstPerson::class, Singular::class).firstOrNull(),
            // делал / сделал
            (forms.with(Past::class, Masculine::class).firstOrNull()
                ?: forms.with(Past::class).without(Participle::class).firstOrNull()
                ?: forms.with(Past::class).firstOrNull()),
        ).distinctBy { it.text }
        Lang.EN -> listOfNotNull(
            // to go
            forms.with(Infinitive::class).firstOrNull(),
            // goes
            forms.with(Present::class, ThirdPerson::class, Singular::class).firstOrNull(),
            // went
            forms.with(Past::class).without(Participle::class).firstOrNull(),
            // gone
            forms.with(Past::class, Participle::class).firstOrNull(),
        )
            // Distinct for English, because most pasts are same (with and without Participle)
            .distinctBy { it.text }
        Lang.DE -> listOfNotNull(
            // gehen
            forms.with(Infinitive::class).firstOrNull(),
            // geht
            forms.with(Present::class, ThirdPerson::class, Singular::class).firstOrNull(),
            // ging
            forms.with(Past::class, ThirdPerson::class, Singular::class, Indicative::class).firstOrNull(),
            // is gegangen
            forms.with(Perfect::class, Indicative::class, ThirdPerson::class, Singular::class).firstOrNull(),
        )
        Lang.FR -> listOfNotNull(
            // faire
            forms.with(Infinitive::class).firstOrNull() ?: forms.with(MainForm::class).firstOrNull(),
            // fait (3sg present)
            // The more pronouns the better, because we want "il/elle/on"
            forms.with(Indicative::class, Present::class).minByOrNull { -it.pronouns.size },
            // faisons (1pl present)
            (forms.with(Present::class, Indicative::class)
                .firstOrNull { it.pronouns.any { it.text == "nous" } }),
            // fait
            (forms.with(Past::class, Participle::class).firstOrNull()
                ?: forms.with(Participle::class).firstOrNull()),
        )
    }
}
