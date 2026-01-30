package blazern.lexisoup.data.lexical_item_details_source.kaikki

import blazern.lexisoup.data.kaikki.model.Entry
import blazern.lexisoup.data.kaikki.model.Form
import blazern.lexisoup.domain.model.Article
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail.Forms
import blazern.lexisoup.domain.model.PartOfSpeech
import blazern.lexisoup.domain.model.PartOfSpeech.Noun
import blazern.lexisoup.domain.model.PartOfSpeech.Verb
import blazern.lexisoup.domain.model.Pronoun
import blazern.lexisoup.domain.model.WordForm
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Dative
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Feminine
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Genitive
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.MainForm
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Masculine
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Neuter
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Nominative
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Plural
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Singular
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainTypesUtilsTest {
    @Test
    fun `the root entry is added as the main form`() = runTest {
        val entry = Entry(
            word = "Haus",
            pos = "noun",
            langCode = "de",
            forms = listOf(
                Form("Häuser", tags = listOf("plural")),
            ),
            tags = listOf("neuter", "nominative")
        )
        val expected = listOf(
            Forms(
                Forms.Value.Detailed(listOf(
                    WordForm("Häuser", Lang.DE, listOf(Plural("plural"))),
                    WordForm(
                        text = "Haus",
                        tags = listOf(Neuter("neuter"), Nominative("nominative"), MainForm()),
                        lang = Lang.DE,
                        // Automatic article
                        articles = setOf(Article.allOf(Lang.DE)["das"]!!)
                    )
                )),
                Lang.DE,
                DataSource.Kaikki,
                pos = Noun,
            ),
        )
        val result = entry.toDetails(langFrom = Lang.DE, langTo = Lang.EN)
        assertEquals(expected, result)
    }

    @Test
    fun `part of speech is parsed`() = runTest {
        val posMap = mapOf(
            "noun" to Noun,
            "verb" to Verb,
            "adjective" to PartOfSpeech.Other("adjective"),
        )
        val entryTemplate = Entry(
            word = "Haus",
            pos = "NONE",
            langCode = "de",
        )
        posMap.forEach { (posStr, _) ->
            val entry = entryTemplate.copy(pos = posStr)
            val result = entry.toDetails(langFrom = Lang.DE, langTo = Lang.EN)
            assertEquals(posMap[posStr], (result.first() as Forms).pos)
        }
    }

    @Test
    fun `auto articles in German`() = runTest {
        val articlesMap = mapOf(
            "neuter" to Article.allOf(Lang.DE)["das"]!!,
            "masculine" to Article.allOf(Lang.DE)["der"]!!,
            "feminine" to Article.allOf(Lang.DE)["die"]!!,
        )
        val tagsMap = mapOf(
            "neuter" to Neuter("neuter"),
            "masculine" to Masculine("masculine"),
            "feminine" to Feminine("feminine"),
        )
        val entryTemplate = Entry(
            word = "Haus",
            pos = "noun",
            langCode = "de",
        )
        val mainFormTemplate = WordForm(
            text = "Haus",
            tags = emptyList(),
            lang = Lang.DE,
        )
        articlesMap.forEach { (genderTxt, article) ->
            val entry = entryTemplate.copy(tags = listOf(genderTxt))
            val mainForm = mainFormTemplate.copy(
                tags = listOf(tagsMap[genderTxt]!!, MainForm()),
                articles = setOf(article),
            )
            val expected = listOf(
                Forms(
                    Forms.Value.Detailed(listOf(mainForm)),
                    Lang.DE,
                    DataSource.Kaikki,
                    pos = Noun,
                ),
            )
            val result = entry.toDetails(langFrom = Lang.DE, langTo = Lang.EN)
            assertEquals(expected, result)
        }
    }

    @Test
    fun `auto articles in French`() = runTest {
        val articlesMap = mapOf(
            "masculine" to Article.allOf(Lang.FR)["le"]!!,
            "feminine" to Article.allOf(Lang.FR)["la"]!!,
        )
        val tagsMap = mapOf(
            "masculine" to Masculine("masculine"),
            "feminine" to Feminine("feminine"),
        )
        val entryTemplate = Entry(
            word = "maison",
            pos = "noun",
            langCode = "fr",
        )
        val mainFormTemplate = WordForm(
            text = "maison",
            tags = emptyList(),
            lang = Lang.FR,
        )
        articlesMap.forEach { (genderTxt, article) ->
            val entry = entryTemplate.copy(tags = listOf(genderTxt))
            val mainForm = mainFormTemplate.copy(
                tags = listOf(tagsMap[genderTxt]!!, MainForm()),
                articles = setOf(article),
            )
            val expected = listOf(
                Forms(
                    Forms.Value.Detailed(listOf(mainForm)),
                    Lang.FR,
                    DataSource.Kaikki,
                    pos = Noun,
                ),
            )
            val result = entry.toDetails(langFrom = Lang.FR, langTo = Lang.EN)
            assertEquals(expected, result)
        }
    }

    @Test
    fun `articles are parsed`() = runTest {
        val entry = Entry(
            word = "Hund",
            pos = "noun",
            langCode = "de",
            forms = listOf(
                Form("der Hund", tags = listOf("singular", "nominative")),
                Form("dem Hund", tags = listOf("singular", "dative")),
                Form("des Hundes", tags = listOf("singular", "genitive")),
                Form("den Hunden", tags = listOf("plural", "dative")),
            ),
        )
        val expected = listOf(
            Forms(
                Forms.Value.Detailed(listOf(
                    WordForm(
                        "Hund",
                        Lang.DE,
                        listOf(Singular("singular"), Nominative("nominative")),
                        articles = setOf(Article.allOf(Lang.DE)["der"]!!),
                        textRaw = "der Hund",
                    ),
                    WordForm(
                        "Hund",
                        Lang.DE,
                        listOf(Singular("singular"), Dative("dative")),
                        articles = setOf(Article.allOf(Lang.DE)["dem"]!!),
                        textRaw = "dem Hund",
                    ),
                    WordForm(
                        "Hundes",
                        Lang.DE,
                        listOf(Singular("singular"), Genitive("genitive")),
                        articles = setOf(Article.allOf(Lang.DE)["des"]!!),
                        textRaw = "des Hundes",
                    ),
                    WordForm(
                        "Hunden",
                        Lang.DE,
                        listOf(Plural("plural"), Dative("dative")),
                        articles = setOf(Article.allOf(Lang.DE)["den"]!!),
                        textRaw = "den Hunden",
                    ),
                    WordForm(
                        text = "Hund",
                        tags = listOf(MainForm()),
                        lang = Lang.DE,
                    )
                )),
                Lang.DE,
                DataSource.Kaikki,
                pos = Noun,
            ),
        )
        val result = entry.toDetails(langFrom = Lang.DE, langTo = Lang.EN)
        assertEquals(expected, result)
    }

    @Test
    fun `pronouns are parsed`() = runTest {
        val entry = Entry(
            word = "gehen",
            pos = "verb",
            langCode = "de",
            forms = listOf(
                Form("gehen"),
                Form("er/sie/es geht"),
                Form("wir gehen"),
                Form("du gehst"),
            ),
        )
        val expected = listOf(
            Forms(
                Forms.Value.Detailed(listOf(
                    WordForm(
                        "gehen",
                        Lang.DE,
                        pronouns = emptySet(),
                    ),
                    WordForm(
                        "geht",
                        Lang.DE,
                        pronouns = setOf(
                            Pronoun.allOf(Lang.DE)["er"]!!,
                            Pronoun.allOf(Lang.DE)["sie"]!!,
                            Pronoun.allOf(Lang.DE)["es"]!!,
                        ),
                        textRaw = "er/sie/es geht"
                    ),
                    WordForm(
                        "gehen",
                        Lang.DE,
                        pronouns = setOf(
                            Pronoun.allOf(Lang.DE)["wir"]!!,
                        ),
                        textRaw = "wir gehen"
                    ),
                    WordForm(
                        "gehst",
                        Lang.DE,
                        pronouns = setOf(
                            Pronoun.allOf(Lang.DE)["du"]!!,
                        ),
                        textRaw = "du gehst"
                    ),
                    WordForm(
                        text = "gehen",
                        tags = listOf(MainForm()),
                        lang = Lang.DE,
                    )
                )),
                Lang.DE,
                DataSource.Kaikki,
                pos = Verb,
            ),
        )
        val result = entry.toDetails(langFrom = Lang.DE, langTo = Lang.EN)
        assertEquals(expected, result)
    }

    @Test
    fun `only letters`() = runTest {
        val entry = Entry(
            word = "komm!! her...",
            pos = "verb",
            langCode = "de",
            forms = listOf(
                Form("komme!"),
            ),
        )
        val expected = listOf(
            Forms(
                Forms.Value.Detailed(listOf(
                    WordForm(
                        text = "komme",
                        lang = Lang.DE,
                        textRaw = "komme!",
                    ),
                    WordForm(
                        text = "komm her",
                        tags = listOf(MainForm()),
                        lang = Lang.DE,
                        textRaw = "komm!! her...",
                        wordsCount = 2,
                    )
                )),
                Lang.DE,
                DataSource.Kaikki,
                pos = Verb,
            ),
        )
        val result = entry.toDetails(langFrom = Lang.DE, langTo = Lang.EN)
        assertEquals(expected, result)
    }
}