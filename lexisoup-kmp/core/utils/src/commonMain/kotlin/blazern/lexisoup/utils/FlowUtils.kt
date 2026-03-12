package blazern.lexisoup.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> Flow<T>.unique(): Flow<T> = flow {
    val seenValues = mutableSetOf<T>()
    collect {
        if (seenValues.add(it)) {
            emit(it)
        }
    }
}
