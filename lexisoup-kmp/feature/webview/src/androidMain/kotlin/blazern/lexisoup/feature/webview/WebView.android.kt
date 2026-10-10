package blazern.lexisoup.feature.webview

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@androidx.compose.runtime.Composable
actual fun WebView(
    html: String,
    modifier: Modifier,
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()

                // The widget does not work otherwise
                @SuppressLint("SetJavaScriptEnabled")
                settings.javaScriptEnabled = true

                loadData(html, "text/html; charset=utf-8", "UTF-8")

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest,
                    ): Boolean {
                        // The UI looks terrible if the WebView navigates to the main
                        // YouGlish page
                        val isYouGlishHome =
                            request.url.host in setOf("youglish.com", "www.youglish.com") &&
                                    request.url.path.orEmpty() in setOf("", "/")
                        return isYouGlishHome
                    }
                }
            }
        }
    )
}
