package app.kvasir.launcher.ui.home

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kvasir.launcher.domain.LetterBucket
import app.kvasir.launcher.domain.model.HomeListMode
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Spec 013 / RF-013-01 + Spec 019 / RF-019-04 + Spec 020 / RF-020-05 —
 * Compact ★ + A–Z + # rail. Drag: continuous sliding bubble + haptic on index change.
 */
@Composable
fun HomeScrubberRail(
    listMode: HomeListMode,
    onSelectFavorites: () -> Unit,
    onSelectLetter: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    val letterKeys = LetterBucket.KEYS
    // Index 0 = favorites star; 1.. = letters.
    val totalSlots = 1 + letterKeys.size
    val view = LocalView.current
    val density = LocalDensity.current
    val bubbleSizePx = with(density) { BUBBLE_SIZE_DP.dp.toPx() }

    var scrubberHeightPx by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragIndex by remember { mutableIntStateOf(-1) }
    var dragYPx by remember { mutableFloatStateOf(0f) }

    fun glyphAt(index: Int): String =
        if (index <= 0) "★" else letterKeys[index - 1].toString()

    fun selectByIndex(index: Int, yPx: Float) {
        val i = index.coerceIn(0, totalSlots - 1)
        dragYPx = yPx
        if (i != dragIndex) {
            // Spec 020 / RF-020-05 — brief haptic only when the slot changes (no VIBRATE perm).
            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            dragIndex = i
            if (i == 0) {
                onSelectFavorites()
            } else {
                onSelectLetter(letterKeys[i - 1])
            }
        }
    }

    fun modeIndex(): Int = when (listMode) {
        is HomeListMode.Favorites -> 0
        is HomeListMode.Letter -> {
            val idx = letterKeys.indexOf(listMode.letter)
            if (idx >= 0) idx + 1 else 0
        }
    }

    fun indexFromY(yPx: Float): Int {
        if (scrubberHeightPx <= 0f) return 0
        val fraction = (yPx / scrubberHeightPx).coerceIn(0f, 1f)
        return (fraction * (totalSlots - 1)).roundToInt()
    }

    val activeIndex = if (isDragging && dragIndex >= 0) dragIndex else modeIndex()
    val curveAmplitudePx = if (isDragging) DRAG_CURVE_AMPLITUDE_PX else IDLE_CURVE_AMPLITUDE_PX
    val dragFraction = if (scrubberHeightPx <= 0f) {
        0.5f
    } else {
        (dragYPx / scrubberHeightPx).coerceIn(0f, 1f)
    }
    val bubbleCurvePx = (-sin(dragFraction * Math.PI) * curveAmplitudePx).roundToInt()
    val bubbleOffsetY = (dragYPx - bubbleSizePx / 2f)
        .coerceIn(0f, (scrubberHeightPx - bubbleSizePx).coerceAtLeast(0f))
        .roundToInt()

    Box(
        modifier = modifier
            .width(RAIL_WIDTH_DP.dp)
            .fillMaxHeight(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Box(
            modifier = Modifier
                .width(GLYPH_COLUMN_WIDTH_DP.dp)
                .padding(vertical = 12.dp)
                .onSizeChanged { scrubberHeightPx = it.height.toFloat() }
                .pointerInput(totalSlots) {
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            selectByIndex(indexFromY(offset.y), offset.y)
                        },
                        onDragEnd = {
                            isDragging = false
                            dragIndex = -1
                        },
                        onDragCancel = {
                            isDragging = false
                            dragIndex = -1
                        },
                        onVerticalDrag = { change, _ ->
                            change.consume()
                            selectByIndex(indexFromY(change.position.y), change.position.y)
                        },
                    )
                },
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(1.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ScrubberGlyph(
                    text = "★",
                    selected = !isDragging && activeIndex == 0,
                    curveIndex = 0,
                    totalSlots = totalSlots,
                    curveAmplitudePx = curveAmplitudePx,
                    onClick = onSelectFavorites,
                )
                letterKeys.forEachIndexed { index, letter ->
                    val slot = index + 1
                    ScrubberGlyph(
                        text = letter.toString(),
                        selected = !isDragging && activeIndex == slot,
                        curveIndex = slot,
                        totalSlots = totalSlots,
                        curveAmplitudePx = curveAmplitudePx,
                        onClick = { onSelectLetter(letter) },
                    )
                }
            }

            // Spec 020 / RF-020-05 — continuous sliding bubble (not cell-snapped).
            if (isDragging && scrubberHeightPx > 0f) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(bubbleCurvePx, bubbleOffsetY) }
                        .size(BUBBLE_SIZE_DP.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = glyphAt(activeIndex),
                        fontSize = 13.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScrubberGlyph(
    text: String,
    selected: Boolean,
    curveIndex: Int,
    totalSlots: Int,
    curveAmplitudePx: Float,
    onClick: () -> Unit,
) {
    val fraction = if (totalSlots <= 1) 0.5f else curveIndex.toFloat() / (totalSlots - 1).toFloat()
    val curvePx = (-sin(fraction * Math.PI) * curveAmplitudePx).roundToInt()

    Box(
        modifier = Modifier
            .offset { IntOffset(curvePx, 0) }
            .size(BUBBLE_SIZE_DP.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            lineHeight = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

private const val IDLE_CURVE_AMPLITUDE_PX = 4f
private const val DRAG_CURVE_AMPLITUDE_PX = 56f
private const val RAIL_WIDTH_DP = 56
private const val GLYPH_COLUMN_WIDTH_DP = 28
private const val BUBBLE_SIZE_DP = 22
