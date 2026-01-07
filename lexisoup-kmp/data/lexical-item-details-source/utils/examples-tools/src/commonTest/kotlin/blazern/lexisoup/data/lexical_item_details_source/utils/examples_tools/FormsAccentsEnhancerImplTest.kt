package blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools

import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TextAccent
import blazern.lexisoup.domain.model.WordForm
import kotlin.test.Test
import kotlin.test.assertEquals

class FormsAccentsEnhancerImplTest {

    private val source = DataSource.TATOEBA

    private fun enhancer(vararg forms: String) =
        FormsAccentsEnhancerImpl(forms.map { WordForm(it, emptyList(), Lang.DE) })

    private fun sentence(
        text: String,
        accents: Set<TextAccent> = emptySet()
    ) = Sentence(text, Lang.EN, source, accents)

    @Test
    fun `adds accents for single exact match`() {
        val e = enhancer("tanzt")
        val s = sentence("er tanzt heute")

        val out = e.enhance(s)

        assertEquals(
            setOf(TextAccent(3, 8)),
            out.textAccents,
        )
    }

    @Test
    fun `case-insensitive matching`() {
        val e = enhancer("TanZT")
        val s = sentence("er TANZT heute")

        val out = e.enhance(s)

        assertEquals(
            setOf(TextAccent(3, 8)),
            out.textAccents,
        )
    }

    @Test
    fun `multiple occurrences of same form are all accented`() {
        val e = enhancer("tanzt")
        val s = sentence("tanzt und tanzt")

        val out = e.enhance(s)

        assertEquals(
            setOf(TextAccent(0, 5), TextAccent(10, 15)),
            out.textAccents,
        )
    }

    @Test
    fun `multiple different forms produce multiple accents`() {
        val e = enhancer("tanz", "heute")
        val s = sentence("ich tanz heute")

        val out = e.enhance(s)

        assertEquals(
            setOf(TextAccent(4, 8), TextAccent(9, 14)),
            out.textAccents,
        )
    }

    @Test
    fun `overlapping possible forms are both added (no special merging)`() {
        val e = enhancer("tanz", "tanzt")
        val s = sentence("er tanzt")

        val out = e.enhance(s)

        assertEquals(
            setOf(TextAccent(3, 7), TextAccent(3, 8)),
            out.textAccents,
        )
    }

    @Test
    fun `empty forms are ignored`() {
        val e = FormsAccentsEnhancerImpl(
            listOf(
                WordForm("", emptyList(), Lang.DE),
                WordForm("tanz", emptyList(), Lang.DE)
            )
        )
        val s = sentence("ich tanz")
        val out = e.enhance(s)
        assertEquals(
            setOf(TextAccent(4, 8)),
            out.textAccents,
        )
    }

    @Test
    fun `duplicate forms do not duplicate accents (set semantics)`() {
        val e = enhancer("tanz", "tanz")
        val s = sentence("tanz")

        val out = e.enhance(s)

        assertEquals(
            setOf(TextAccent(0, 4)),
            out.textAccents,
        )
    }

    @Test
    fun `existing accents are preserved and new ones are added`() {
        val existing = setOf(TextAccent(0, 2))
        val s = sentence("er tanzt heute", existing)

        val e = enhancer("tanzt")
        val out = e.enhance(s)

        val expected = existing + setOf(TextAccent(3, 8))
        assertEquals(expected, out.textAccents)
    }

    @Test
    fun `no matches returns sentence with same accents`() {
        val existing = setOf(TextAccent(0, 1))
        val s = sentence("kein Treffer", existing)

        val e = enhancer("tanzt")
        val out = e.enhance(s)

        assertEquals(existing, out.textAccents)
        assertEquals(out.text, s.text)
    }
}
