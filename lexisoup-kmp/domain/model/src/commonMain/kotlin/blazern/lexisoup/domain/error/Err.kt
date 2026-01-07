package blazern.lexisoup.domain.error

import kotlinx.io.IOException

sealed interface Err {
    val e: Exception?

    sealed interface Net : Err {
        data class IO(override val e: Exception) : Net
    }
    data class Other(override val e: Exception?) : Err

    companion object {
        fun from(e: Exception): Err = when (e) {
            is IOException -> Net.IO(e)
            else -> Other(e)
        }
    }
}
