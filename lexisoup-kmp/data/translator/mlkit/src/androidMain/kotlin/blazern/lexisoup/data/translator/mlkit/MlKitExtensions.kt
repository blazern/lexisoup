package blazern.lexisoup.data.translator.mlkit

import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun RemoteModelManager.getDownloadedModels(): Set<TranslateRemoteModel> =
    withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { cont ->
            getDownloadedModels(TranslateRemoteModel::class.java)
                .addOnSuccessListener {
                    if (cont.isActive) cont.resume(it)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resumeWithException(it)
                }
        }
    }

suspend fun Translator.download() = withContext(Dispatchers.IO) {
    suspendCancellableCoroutine { cont ->
        downloadModelIfNeeded()
            .addOnSuccessListener {
                if (cont.isActive) cont.resume(Unit)
            }
            .addOnFailureListener { exception ->
                if (cont.isActive) cont.resumeWithException(exception)
            }
    }
}

suspend fun Translator.translateSuspend(text: String): String = withContext(Dispatchers.IO) {
    suspendCancellableCoroutine { cont ->
        translate(text)
            .addOnSuccessListener {
                if (cont.isActive) cont.resume(it)
            }
            .addOnFailureListener { exception ->
                if (cont.isActive) cont.resumeWithException(exception)
            }
    }
}