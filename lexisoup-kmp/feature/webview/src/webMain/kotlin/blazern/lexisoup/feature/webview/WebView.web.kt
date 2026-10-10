package blazern.lexisoup.feature.webview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLIFrameElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.MessageEvent
import org.w3c.dom.MessageEventInit
import org.w3c.dom.events.WheelEvent
import org.w3c.dom.events.WheelEventInit
import org.w3c.dom.events.EventListener
import kotlin.js.unsafeCast

// NOTE: LLM generated
@androidx.compose.runtime.Composable
actual fun WebView(html: String, modifier: Modifier) {
    val contentHeight = remember { mutableStateOf(80.dp) }
    // Keep the iframe connected: WebElementView reorders DOM nodes during
    // placement, which reloads iframe documents when content height changes.
    val frame = remember {
        (document.createElement("iframe") as HTMLIFrameElement).apply {
            style.border = "0"
            style.position = "sticky"
            style.top = "0"
            style.display = "block"
            style.width = "100%"
            style.height = "80px"
            style.visibility = "hidden"
            setAttribute("title", "Embedded web content")
            setAttribute("allow", "autoplay; encrypted-media; fullscreen")
            // Forms are needed if YouGlish displays a verification page.
            setAttribute("sandbox", "allow-scripts allow-same-origin allow-forms")
            // Browsers cannot intercept navigation in cross-origin frames.
            // This guard only covers links in the supplied HTML document.
            srcdoc = """
                <!DOCTYPE html>
                <html>
                <head><meta name="viewport" content="width=device-width, initial-scale=1"></head>
                <body style="margin:0">
                <script>
                document.addEventListener('click', function(event) {
                    var link = event.target.closest && event.target.closest('a[href]');
                    if (!link) return;
                    var url = new URL(link.href, document.baseURI);
                    if ((url.hostname === 'youglish.com' || url.hostname === 'www.youglish.com') &&
                        (url.pathname === '' || url.pathname === '/')) {
                        event.preventDefault();
                        event.stopImmediatePropagation();
                    }
                }, true);
                </script>
                <div id="lexisoup-content" style="display:flow-root;padding:8px">
                $html
                </div>
                <script>
                (function() {
                    var content = document.getElementById('lexisoup-content');
                    var lastHeight = 0;
                    function reportHeight() {
                        // Content height can shrink; document.scrollHeight cannot
                        // shrink below the iframe's current viewport height.
                        if (window.innerWidth <= 0 || window.innerHeight <= 0) {
                            return;
                        }
                        var height = Math.max(1, Math.ceil(content.getBoundingClientRect().height));
                        if (height !== lastHeight) {
                            lastHeight = height;
                            window.parent.postMessage(height, '*');
                        }
                    }
                    new ResizeObserver(reportHeight).observe(content);
                    window.addEventListener('load', reportHeight);
                    window.addEventListener('resize', reportHeight);
                    reportHeight();
                })();
                </script>
                </body>
                </html>
            """.trimIndent()
        }
    }
    // Native scroll chaining crosses iframe boundaries even though DOM wheel
    // events do not. Keep the iframe sticky in a scrollable host, and translate
    // the host's scroll delta into the wheel events consumed by Compose.
    val scrollHost = remember {
        (document.createElement("div") as HTMLDivElement).apply {
            style.position = "fixed"
            style.visibility = "hidden"
            style.width = "100%"
            style.height = "80px"
            style.setProperty("overflow-y", "scroll")
            style.setProperty("overflow-x", "hidden")
            style.setProperty("scrollbar-width", "none")
            style.setProperty("overflow-anchor", "none")
            style.setProperty("overscroll-behavior", "contain")
            val scrollContent = document.createElement("div") as HTMLDivElement
            scrollContent.appendChild(frame)
            val spacer = document.createElement("div") as HTMLDivElement
            spacer.style.height = "16384px"
            spacer.setAttribute("aria-hidden", "true")
            scrollContent.appendChild(spacer)
            appendChild(scrollContent)
        }
    }
    val wheelPosition = remember { intArrayOf(0, 0) }
    DisposableEffect(frame) {
        val listener = EventListener { event ->
            val message = event as? MessageEvent
            val frameWindow = frame.contentWindow
            // YouGlish normally messages its parent, but its verification/error
            // page messages window.top. Route only messages from this widget's
            // own YouGlish frame back to the embedded widget listener.
            if (message != null && frameWindow != null && message.origin == "https://youglish.com") {
                val children = frame.contentDocument?.querySelectorAll("iframe")
                for (index in 0 until (children?.length ?: 0)) {
                    // Elements in srcdoc use a different realm's constructors,
                    // so instanceof/as? against the host constructor fails.
                    val child = children?.item(index)?.unsafeCast<HTMLIFrameElement>() ?: continue
                    if (child.contentWindow !== message.source) continue
                    frameWindow.dispatchEvent(
                        MessageEvent("message", MessageEventInit(
                            data = message.data,
                            origin = message.origin,
                            source = message.source,
                        ))
                    )
                    break
                }
            }
            if (message != null && frameWindow != null && message.source === frameWindow) {
                val height = (message.data as? Number)?.toDouble()
                if (height != null && height.isFinite() && height > 0) {
                    contentHeight.value = height.toFloat().dp
                }
            }
        }
        window.addEventListener("message", listener)
        // ComposeViewport gives body a shadow root without slots. Light-DOM
        // children still load, but never render, yielding a zero-sized viewport.
        val body = document.body
        val mountPoint = body?.shadowRoot ?: body
        mountPoint?.appendChild(scrollHost)
        val scrollCenter = 8192.0
        scrollHost.scrollTop = scrollCenter
        val scrollListener = EventListener {
            val delta = scrollHost.scrollTop - scrollCenter
            if (delta != 0.0) {
                // Reset before dispatch: Compose may synchronously update layout.
                scrollHost.scrollTop = scrollCenter
                val canvas = mountPoint?.querySelector("canvas")
                canvas?.dispatchEvent(WheelEvent("wheel", WheelEventInit(
                    deltaY = delta,
                    deltaMode = 0,
                    clientX = wheelPosition[0],
                    clientY = wheelPosition[1],
                    bubbles = true,
                    cancelable = true,
                )))
            }
        }
        scrollHost.addEventListener("scroll", scrollListener)
        onDispose {
            window.removeEventListener("message", listener)
            scrollHost.removeEventListener("scroll", scrollListener)
            scrollHost.parentNode?.removeChild(scrollHost)
            frame.srcdoc = ""
        }
    }
    val density = LocalDensity.current.density
    Box(
        modifier = modifier.fillMaxWidth().height(contentHeight.value)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInWindow()
                val visible = coordinates.boundsInWindow()
                // Keep forwarded wheel coordinates inside the visible list item,
                // including when a large widget extends beyond the viewport.
                wheelPosition[0] = (visible.center.x / density).toInt()
                wheelPosition[1] = (visible.center.y / density).toInt()
                val width = coordinates.size.width / density
                val height = coordinates.size.height / density
                val left = position.x / density
                val top = position.y / density
                scrollHost.style.left = "${left}px"
                scrollHost.style.top = "${top}px"
                scrollHost.style.width = "${width}px"
                scrollHost.style.height = "${height}px"
                frame.style.height = "${height}px"
                // Clip to the visible part of the LazyColumn item.
                val clipTop = ((visible.top - position.y) / density).coerceAtLeast(0f)
                val clipLeft = ((visible.left - position.x) / density).coerceAtLeast(0f)
                val clipBottom = (height - (visible.bottom - position.y) / density).coerceAtLeast(0f)
                val clipRight = (width - (visible.right - position.x) / density).coerceAtLeast(0f)
                scrollHost.style.setProperty(
                    "clip-path", "inset(${clipTop}px ${clipRight}px ${clipBottom}px ${clipLeft}px)"
                )
                frame.style.visibility = if (visible.isEmpty) "hidden" else "visible"
                scrollHost.style.visibility = frame.style.visibility
            },
    )
}
