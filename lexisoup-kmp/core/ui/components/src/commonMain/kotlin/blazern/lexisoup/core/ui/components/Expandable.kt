package blazern.lexisoup.core.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import blazern.lexisoup.core.ui.theme.LexisoupTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.exp
import kotlin.math.min

// ChatGPT-generated **
/**
 * Expands only on clicking the bottom fade area. No clicks if no overflow.
 * Optional [control] slot can render an expand/collapse button.
 *
 * @param control A composable placed on top of the content. You get:
 *   - [expanded] current state
 *   - [canExpand] whether content overflows
 *   - [onToggle] to toggle expansion
 *   Use BoxScope to position it (e.g. align BottomEnd).
 */
@Suppress("LongMethod", "MaxLineLength")
@Composable
fun Expandable(
    modifier: Modifier = Modifier,
    collapsedMaxHeight: Dp = 128.dp,
    fadeHeight: Dp = 64.dp,
    initiallyExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    control: (@Composable BoxScope.(expanded: Boolean, canExpand: Boolean, onToggle: () -> Unit) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }

    Box(modifier = modifier.animateContentSize()) {
        SubcomposeLayout { constraints ->
            val collapsedMaxPx = collapsedMaxHeight.roundToPx()

            // 1) Measure full (unbounded height) to know if it overflows
            val fullPlaceables = subcompose("full", content).map {
                it.measure(
                    androidx.compose.ui.unit.Constraints(
                        minWidth = constraints.minWidth,
                        maxWidth = constraints.maxWidth,
                        minHeight = 0,
                        maxHeight = androidx.compose.ui.unit.Constraints.Infinity
                    )
                )
            }
            val fullH = fullPlaceables.maxOfOrNull { it.height } ?: 0
            val overflows = fullH > collapsedMaxPx

            // 2) Choose collapsed (with mask) or full content
            val finalPlaceables =
                if (expanded || !overflows) {
                    fullPlaceables
                } else {
                    val collapsedConstraints = androidx.compose.ui.unit.Constraints(
                        minWidth = constraints.minWidth,
                        maxWidth = constraints.maxWidth,
                        minHeight = 0,
                        maxHeight = min(collapsedMaxPx, constraints.maxHeight)
                    )
                    subcompose("collapsed") {
                        Box(
                            Modifier
                                .clipToBounds()
                                .bottomFadeMask(fadeHeight) // your mask (linear or exponential)
                        ) { content() }
                    }.map { it.measure(collapsedConstraints) }
                }

            var w = (finalPlaceables.maxOfOrNull { it.width } ?: 0)
                .coerceIn(constraints.minWidth, constraints.maxWidth)
            var h = (finalPlaceables.maxOfOrNull { it.height } ?: 0)
                .coerceIn(constraints.minHeight, constraints.maxHeight)

            // 3) Overlay: bottom clickable fade area (only when collapsed & can expand)
            //    + optional control button slot
            val overlayPlaceables = subcompose("overlay") {
                Box(Modifier.fillMaxSize()) {
                    val toggle = {
                        val new = !expanded
                        expanded = new
                        onExpandedChange(new)
                    }

                    // Only this area is clickable to expand
                    if (!expanded && overflows) {
                        Box(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .height(fadeHeight)
                                .clickable { toggle() } // no click registered if not composed
                        )
                    }

                    // Optional control (e.g., expand/collapse button)
                    control?.invoke(this, expanded, overflows, toggle)
                }
            }.map {
                // Overlay should match the final content size
                it.measure(
                    androidx.compose.ui.unit.Constraints.fixed(
                        w.coerceAtLeast(0),
                        h.coerceAtLeast(0)
                    )
                )
            }

            // Final layout
            layout(w, h) {
                finalPlaceables.forEach { it.place(0, 0) }
                overlayPlaceables.forEach { it.place(0, 0) } // on top
            }
        }
    }
}

// ChatGPT-generated
@Suppress("LongMethod", "MaxLineLength", "MagicNumber")
private fun Modifier.bottomFadeMask(
    fadeHeight: Dp = 24.dp,
    strength: Float = 4f,          // larger = steeper fade
    preOpaqueOverlap: Dp = 1.dp,   // small plateau overlap to hide seam
    steps: Int = 32                // more steps = smoother curve
): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithCache {
        val h = size.height
        if (h <= 0f) return@drawWithCache onDrawWithContent { drawContent() }

        val fadePx = fadeHeight.toPx().coerceAtMost(h)
        val prePx = preOpaqueOverlap.toPx().coerceIn(0f, fadePx * 0.8f)
        // Fraction (0..1) where we keep alpha==1 (slightly into the fade band)
        val plateauEnd = ((h - fadePx) + prePx).coerceIn(0f, h) / h

        // Exponential alpha on the ramp: a(t) in [0..1], t in [0..1]
        val k = strength.coerceAtLeast(1e-3f)
        val eNegK = exp(-k)

        val stops = buildList<Pair<Float, Color>> {
            // Full opacity across the top + small overlap
            add(0f to Color.Black)
            add(plateauEnd to Color.Black)

            // Exponential ramp for the remaining part
            for (i in 0..steps) {
                val t = i / steps.toFloat() // 0..1 across ramp
                val alpha = ((exp(-k * t) - eNegK) / (1f - eNegK))
                    .coerceIn(0f, 1f)
                val pos = plateauEnd + (1f - plateauEnd) * t
                add(pos.coerceIn(0f, 1f) to Color.Black.copy(alpha = alpha))
            }
        }.distinctBy { it.first }.toTypedArray()

        val mask = Brush.verticalGradient(colorStops = stops)

        onDrawWithContent {
            drawContent()
            drawRect(brush = mask, size = size, blendMode = BlendMode.DstIn)
        }
    }



@Preview
@Composable
private fun PreviewSmall() {
    LexisoupTheme {
        Box(modifier = Modifier.height(256.dp)) {
            Expandable {
                Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
            }
        }
    }
}

@Suppress("MaxLineLength")
@Preview
@Composable
private fun PreviewBig() {
    LexisoupTheme {
        Box(modifier = Modifier.height(256.dp)) {
            Expandable {
                Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
            }
        }
    }
}