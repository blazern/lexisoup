package blazern.lexisoup.feature.search_results.repository

import blazern.lexisoup.core.logging.Log
import blazern.lexisoup.domain.error.Err
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class BackgroundErrorsRepository {
    private val _errors = MutableSharedFlow<Err>()
    val errors = _errors.asSharedFlow()

    fun emit(err: Err) {
        Log.e(TAG, err.e) { "Background error: $err" }
        _errors.tryEmit(err)
    }

    private companion object {
        const val TAG = "BackgroundErrorsRepository"
    }
}
