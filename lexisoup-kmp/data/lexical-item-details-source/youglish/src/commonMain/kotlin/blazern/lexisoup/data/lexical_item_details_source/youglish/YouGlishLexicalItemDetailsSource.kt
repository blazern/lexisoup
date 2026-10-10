package blazern.lexisoup.data.lexical_item_details_source.youglish

import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item.Page
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import io.ktor.http.encodeURLParameter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class YouGlishLexicalItemDetailsSource : LexicalItemDetailsSource {
    override val source = DataSource.YouGlish
    override val types = setOf(LexicalItemDetail.Type.PRONUNCIATION)

    override fun request(
        query: String,
        langFrom: Lang,
        langTo: Lang
    ): Flow<LexicalItemDetailsSource.Item> {
        // NOTE: there's no way to know if YouGlish actually has the the given word in their database
        val html = """
            <a id="yg-widget-0" class="youglish-widget" data-query="${query.encodeURLParameter()}" data-lang="${langFrom.toYouGlishLang()}" data-components="8415" data-bkg-color="theme_light"  rel="nofollow" href="https://youglish.com">Visit YouGlish.com</a>
            <script async src="https://youglish.com/public/emb/widget.js" charset="utf-8"></script>
        """.trimIndent()
        return flowOf(Page(
            listOf(LexicalItemDetail.Pronunciation.HTML(html, source)),
            types,
        ))
    }
}

private fun Lang.toYouGlishLang() = when (this) {
    Lang.RU -> "russian"
    Lang.EN -> "english"
    Lang.DE -> "german"
    Lang.FR -> "french"
}
