package blazern.lexisoup.domain.model

import androidx.compose.runtime.Composable
import blazern.lexisoup.core.ui.strings.stringResource
import lexisoup.core.ui.strings.generated.resources.Res
import lexisoup.core.ui.strings.generated.resources.general_pos_adj
import lexisoup.core.ui.strings.generated.resources.general_pos_adv
import lexisoup.core.ui.strings.generated.resources.general_pos_article
import lexisoup.core.ui.strings.generated.resources.general_pos_conj
import lexisoup.core.ui.strings.generated.resources.general_pos_det
import lexisoup.core.ui.strings.generated.resources.general_pos_inj
import lexisoup.core.ui.strings.generated.resources.general_pos_noun
import lexisoup.core.ui.strings.generated.resources.general_pos_num
import lexisoup.core.ui.strings.generated.resources.general_pos_particle
import lexisoup.core.ui.strings.generated.resources.general_pos_prep
import lexisoup.core.ui.strings.generated.resources.general_pos_pron
import lexisoup.core.ui.strings.generated.resources.general_pos_verb


sealed interface PartOfSpeech {
    object Noun : PartOfSpeech
    object Verb : PartOfSpeech
    object Preposition : PartOfSpeech
    object Pronoun : PartOfSpeech
    object Adjective : PartOfSpeech
    object Adverb : PartOfSpeech
    object Conjunction : PartOfSpeech
    object Determiner : PartOfSpeech
    object Article : PartOfSpeech
    object Numeral : PartOfSpeech
    object Particle : PartOfSpeech
    object Interjection : PartOfSpeech

    data class Other(
        val raw: String,
    ) : PartOfSpeech
}

@Composable
fun PartOfSpeech.i18n(): String = when (this) {
    PartOfSpeech.Noun -> stringResource(
        Res.string.general_pos_noun,
        preview = this::class.simpleName.toString(),
    )
    PartOfSpeech.Verb -> stringResource(
        Res.string.general_pos_verb,
        preview = this::class.simpleName.toString(),
    )
    PartOfSpeech.Preposition -> stringResource(
        Res.string.general_pos_prep,
        preview = this::class.simpleName.toString(),
    )
    PartOfSpeech.Pronoun -> stringResource(
        Res.string.general_pos_pron,
        preview = this::class.simpleName.toString(),
    )
    PartOfSpeech.Adjective -> stringResource(
        Res.string.general_pos_adj,
        preview = this::class.simpleName.toString(),
    )
    PartOfSpeech.Adverb -> stringResource(
        Res.string.general_pos_adv,
        preview = this::class.simpleName.toString(),
    )
    PartOfSpeech.Conjunction -> stringResource(
        Res.string.general_pos_conj,
        preview = this::class.simpleName.toString(),
    )
    PartOfSpeech.Article -> stringResource(
        Res.string.general_pos_article,
        preview = this::class.simpleName.toString(),
    )
    PartOfSpeech.Determiner -> stringResource(
        Res.string.general_pos_det,
        preview = this::class.simpleName.toString(),
    )
    PartOfSpeech.Numeral -> stringResource(
        Res.string.general_pos_num,
        preview = this::class.simpleName.toString(),
    )
    PartOfSpeech.Particle -> stringResource(
        Res.string.general_pos_particle,
        preview = this::class.simpleName.toString(),
    )
    PartOfSpeech.Interjection -> stringResource(
        Res.string.general_pos_inj,
        preview = this::class.simpleName.toString(),
    )
    is PartOfSpeech.Other -> raw
}