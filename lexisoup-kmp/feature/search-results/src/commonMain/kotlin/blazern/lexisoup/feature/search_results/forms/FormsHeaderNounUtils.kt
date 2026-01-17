package blazern.lexisoup.feature.search_results.forms

import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.WordForm
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Genitive
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.MainForm
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Nominative
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Plural
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Singular

internal fun selectNounFormsForHeader(
    forms: List<WordForm>,
    lang: Lang,
): List<WordForm> {
    return when (lang) {
        Lang.EN -> listOfNotNull(
            // child
            forms.with(MainForm::class).firstOrNull() ?: forms.with(Singular::class).firstOrNull(),
            // children
            forms.with(Plural::class).firstOrNull(),
        )
        Lang.DE -> listOfNotNull(
            // das Kind
            forms.with(MainForm::class).firstOrNull() ?: forms.with(Singular::class).firstOrNull(),
            // die Kinder
            forms.with(Plural::class).firstOrNull(),
        )
        Lang.RU -> listOfNotNull(
            // стол
            (forms.with(MainForm::class).firstOrNull()
                ?: forms.with(Nominative::class, Singular::class).firstOrNull()
                ?: forms.with(Singular::class).firstOrNull()),
            // стола
            forms.with(Genitive::class, Singular::class).firstOrNull(),
            // столы
            forms.with(Nominative::class, Plural::class).firstOrNull()
                ?: forms.with(Plural::class).firstOrNull(),
            // столов
            forms.with(Genitive::class, Plural::class).firstOrNull(),
        )
        Lang.FR -> listOfNotNull(
            // chat
            forms.with(MainForm::class).firstOrNull()
                ?: forms.with(Singular::class).firstOrNull(),
            // chats
            forms.with(Plural::class).firstOrNull(),
        )
    }
}
