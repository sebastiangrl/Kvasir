package app.kvasir.launcher.ui.gesture

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private val SwipeThreshold = 56.dp

/**
 * Spec 004 / RF-004-01, RF-004-02 — vertical swipe with ~56 dp threshold.
 * Does not call change.consume() so scrollable children can still scroll.
 */
fun Modifier.onVerticalSwipe(
    onSwipeUp: (() -> Unit)? = null,
    onSwipeDown: (() -> Unit)? = null,
): Modifier = composed {
    val density = LocalDensity.current
    val thresholdPx = remember(density) { with(density) { SwipeThreshold.toPx() } }
    pointerInput(thresholdPx, onSwipeUp, onSwipeDown) {
        var totalDrag = 0f
        detectVerticalDragGestures(
            onDragStart = { totalDrag = 0f },
            onDragEnd = {
                when {
                    onSwipeUp != null && totalDrag <= -thresholdPx -> onSwipeUp()
                    onSwipeDown != null && totalDrag >= thresholdPx -> onSwipeDown()
                }
                totalDrag = 0f
            },
            onDragCancel = { totalDrag = 0f },
            onVerticalDrag = { _, dragAmount ->
                totalDrag += dragAmount
            },
        )
    }
}
