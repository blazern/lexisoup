package blazern.lexisoup.feature.search_results.usecases

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import blazern.lexisoup.data.translator.api.Translator
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.feature.search_results.model.translatableSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

internal interface TranslateDetailsUseCase {
    operator fun invoke(
        details: List<LexicalItemDetail>,
        translator: Translator,
        langFrom: Lang,
        langTo: Lang,
    ): Flow<Either<Err, LexicalItemDetail>>
}

internal class TranslateDetailsUseCaseImpl(
    private val canTranslate: CanTranslateUseCase
) : TranslateDetailsUseCase {
    override operator fun invoke(
        details: List<LexicalItemDetail>,
        translator: Translator,
        langFrom: Lang,
        langTo: Lang,
    ): Flow<Either<Err, LexicalItemDetail>> = flow {
        val batchSize = translator.capabilities.first().translateBatchSizeLimit

        val errors = mutableListOf<Err>()

        val translatableIndexed =
            details.mapIndexedNotNull { index, detail ->
                val translatable = detail.translatableSet?.original
                if (translatable != null && canTranslate(detail, translator, langFrom, langTo)) {
                    index to translatable
                } else {
                    null
                }
            }

        val translations = buildMap {
            translatableIndexed
                .chunked(batchSize)
                .forEach { batch ->
                    putAll(translateBatch(batch, translator, langFrom, langTo, errors))
                    if (errors.isNotEmpty()) {
                        errors.forEach { emit(Left(it)) }
                        return@flow
                    }
                }
        }

        val detailsTranslated = details.mapIndexed { index, original ->
            translations[index]?.let { original.copyWith(it) } ?: original
        }
        detailsTranslated.forEach { emit(Right(it)) }
    }

    private suspend fun translateBatch(
        batch: List<Pair<Int, Sentence>>,
        translator: Translator,
        langFrom: Lang,
        langTo: Lang,
        errors: MutableList<Err>,
    ): Map<Int, TranslationsSet> {
        val result = mutableMapOf<Int, TranslationsSet>()

        translator.translate(
            sentences = batch.map { it.second },
            langFrom = langFrom,
            langTo = langTo,
        )
            .onEach { it.onLeft { errors += it } }
            .takeWhile { it.isRight() }
            .mapNotNull { it.getOrNull() }
            .collectIndexed { index, translation ->
                val originalIndex = batch[index].first
                result[originalIndex] = translation
            }

        return result
    }
}

private fun LexicalItemDetail.copyWith(newTranslations: TranslationsSet): LexicalItemDetail {
    return when (this) {
        is LexicalItemDetail.Explanation -> copy(translationsSet = newTranslations)
        is LexicalItemDetail.Example -> copy(translationsSet = newTranslations)
        else -> error("Unexpected translatable detail: $this")
    }
}
