package blazern.lexisoup.feature.webview

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSNumber
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationActionPolicy.WKNavigationActionPolicyAllow
import platform.WebKit.WKNavigationActionPolicy.WKNavigationActionPolicyCancel
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.darwin.NSObject

// NOTE: LLM generated
@OptIn(ExperimentalForeignApi::class)
@androidx.compose.runtime.Composable
actual fun WebView(html: String, modifier: androidx.compose.ui.Modifier) {
    val contentHeight = remember { mutableStateOf(1.dp) }
    val heightHandler = remember {
        object : NSObject(), WKScriptMessageHandlerProtocol {
            override fun userContentController(
                userContentController: WKUserContentController,
                didReceiveScriptMessage: WKScriptMessage,
            ) {
                if (!didReceiveScriptMessage.frameInfo.mainFrame) return
                val height = (didReceiveScriptMessage.body as? NSNumber)?.doubleValue ?: return
                if (height.isFinite() && height > 0) {
                    // With a device-width viewport, CSS pixels correspond to iOS points (dp).
                    contentHeight.value = height.toFloat().dp
                }
            }
        }
    }
    // WKWebView holds its delegate weakly; keep it alive in composition.
    val delegate = remember {
        object : NSObject(), WKNavigationDelegateProtocol {
            override fun webView(
                webView: WKWebView,
                decidePolicyForNavigationAction: WKNavigationAction,
                decisionHandler: (WKNavigationActionPolicy) -> Unit,
            ) {
                val url = decidePolicyForNavigationAction.request.URL
                val isYouGlishHome =
                    url?.host in setOf("youglish.com", "www.youglish.com") &&
                        url?.path.orEmpty() in setOf("", "/")
                decisionHandler(
                    if (isYouGlishHome) WKNavigationActionPolicyCancel
                    else WKNavigationActionPolicyAllow
                )
            }
        }
    }
    UIKitView(
        modifier = modifier.fillMaxWidth().height(contentHeight.value),
        factory = {
            val configuration = WKWebViewConfiguration().apply {
                userContentController.addScriptMessageHandler(heightHandler, name = "contentHeight")
            }
            WKWebView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0), configuration = configuration).apply {
                navigationDelegate = delegate
                scrollView.scrollEnabled = false
                // JavaScript is enabled by default, as required by the widget.
                loadHTMLString(
                    """
                        <!DOCTYPE html>
                        <html>
                        <head><meta name="viewport" content="width=device-width, initial-scale=1"></head>
                        <body style="margin:0">
                        <div id="lexisoup-content" style="display:flow-root;padding:8px">
                        $html
                        </div>
                        <script>
                        (function() {
                            var content = document.getElementById('lexisoup-content');
                            var lastHeight = 0;
                            function reportHeight() {
                                // Measure the content, not document.scrollHeight: the latter
                                // is at least the viewport height and prevents shrinking.
                                var height = Math.max(1, Math.ceil(content.getBoundingClientRect().height));
                                if (height !== lastHeight) {
                                    lastHeight = height;
                                    window.webkit.messageHandlers.contentHeight.postMessage(height);
                                }
                            }
                            new ResizeObserver(reportHeight).observe(content);
                            window.addEventListener('load', reportHeight);
                            reportHeight();
                        })();
                        </script>
                        </body>
                        </html>
                    """.trimIndent(),
                    baseURL = null,
                )
            }
        },
        onRelease = {
            it.stopLoading()
            it.navigationDelegate = null
            it.configuration.userContentController.removeScriptMessageHandlerForName("contentHeight")
        },
    )
}
