package blazern.lexisoup.domain.model

import blazern.lexisoup.domain.model.LexicalItemDetail.Example
import blazern.lexisoup.domain.model.LexicalItemDetail.Explanation
import blazern.lexisoup.domain.model.LexicalItemDetail.Forms
import blazern.lexisoup.domain.model.LexicalItemDetail.Synonyms
import blazern.lexisoup.domain.model.LexicalItemDetail.WordTranslations
import lexisoup.core.ui.strings.generated.resources.Res
import lexisoup.core.ui.strings.generated.resources.general_lexical_item_detail_type_example
import lexisoup.core.ui.strings.generated.resources.general_lexical_item_detail_type_explanation
import lexisoup.core.ui.strings.generated.resources.general_lexical_item_detail_type_forms
import lexisoup.core.ui.strings.generated.resources.general_lexical_item_detail_type_synonyms
import lexisoup.core.ui.strings.generated.resources.general_lexical_item_detail_type_word_translations
import org.jetbrains.compose.resources.StringResource
import kotlin.reflect.KClass

sealed class LexicalItemDetail(
    val type: Type,
) {
    /**
     * Non-null if this [LexicalItemDetail] belongs to a group (e.g. to a
     * particular meaning of a the target word).
     */
    abstract val meaningId: String?
    abstract val source: DataSource

    data class Forms(
        val value: Value,
        override val source: DataSource,
        override val meaningId: String? = null,
    ) : LexicalItemDetail(Type.FORMS) {
        sealed class Value {
            data class Text(val text: String) : Value()
            data class Detailed(val forms: List<WordForm>) : Value()
        }
    }

    data class WordTranslations(
        val translationsSet: TranslationsSet,
        override val source: DataSource,
        override val meaningId: String? = null,
    ) : LexicalItemDetail(Type.WORD_TRANSLATIONS)

    data class Synonyms(
        val translationsSet: TranslationsSet,
        override val source: DataSource,
        override val meaningId: String? = null,
    ) : LexicalItemDetail(Type.SYNONYMS)

    data class Explanation(
        val text: String,
        override val source: DataSource,
        override val meaningId: String? = null,
    ) : LexicalItemDetail(Type.EXPLANATION)

    data class Example(
        val translationsSet: TranslationsSet,
        override val source: DataSource,
        override val meaningId: String? = null,
    ) : LexicalItemDetail(Type.EXAMPLE)

    enum class Type {
        FORMS,
        WORD_TRANSLATIONS,
        SYNONYMS,
        EXPLANATION,
        EXAMPLE,
    }

    companion object
}

fun LexicalItemDetail.Type.toClass(): KClass<out LexicalItemDetail> {
    return when (this) {
        LexicalItemDetail.Type.FORMS -> Forms::class
        LexicalItemDetail.Type.WORD_TRANSLATIONS -> WordTranslations::class
        LexicalItemDetail.Type.SYNONYMS -> Synonyms::class
        LexicalItemDetail.Type.EXPLANATION -> Explanation::class
        LexicalItemDetail.Type.EXAMPLE -> Example::class
    }
}

fun LexicalItemDetail.Companion.toType(clazz: KClass<out LexicalItemDetail>): LexicalItemDetail.Type {
    return when (clazz) {
        Forms::class -> LexicalItemDetail.Type.FORMS
        WordTranslations::class -> LexicalItemDetail.Type.WORD_TRANSLATIONS
        Synonyms::class -> LexicalItemDetail.Type.SYNONYMS
        Explanation::class -> LexicalItemDetail.Type.EXPLANATION
        Example::class -> LexicalItemDetail.Type.EXAMPLE
        else -> throw NotImplementedError("Please support LexicalItemDetail subclass: $clazz")
    }
}

val LexicalItemDetail.Type.strRsc: StringResource
    get() {
        return when (this) {
            LexicalItemDetail.Type.FORMS -> Res.string.general_lexical_item_detail_type_forms
            LexicalItemDetail.Type.WORD_TRANSLATIONS -> Res.string.general_lexical_item_detail_type_word_translations
            LexicalItemDetail.Type.SYNONYMS -> Res.string.general_lexical_item_detail_type_synonyms
            LexicalItemDetail.Type.EXPLANATION -> Res.string.general_lexical_item_detail_type_explanation
            LexicalItemDetail.Type.EXAMPLE -> Res.string.general_lexical_item_detail_type_example
        }
    }
