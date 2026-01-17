package blazern.lexisoup.feature.search_results.forms

import blazern.lexisoup.domain.model.Gender
import blazern.lexisoup.domain.model.WordForm
import kotlin.reflect.KClass

internal fun extractGenderFrom(forms: List<WordForm>): Gender? {
    forms.forEach { form ->
        form.tags.forEach { tag ->
            when (tag) {
                is WordForm.Tag.Defined.Masculine -> return Gender.MASCULINE
                is WordForm.Tag.Defined.Feminine -> return Gender.FEMININE
                is WordForm.Tag.Defined.Neuter -> return Gender.NEUTER
                else -> {}
            }
        }
    }
    return null
}

internal fun WordForm.has(
    vararg types: KClass<out WordForm.Tag.Defined>
): Boolean = types.all { type -> this.tags.any { type.isInstance(it) } }

internal fun List<WordForm>.with(
    vararg types: KClass<out WordForm.Tag.Defined>
): List<WordForm> = filter { it.has(*types) }

internal fun List<WordForm>.without(
    vararg types: KClass<out WordForm.Tag.Defined>
): List<WordForm> = filter { !it.has(*types) }
