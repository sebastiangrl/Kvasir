package app.kvasir.launcher.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.kvasir.launcher.domain.LetterBucket
import app.kvasir.launcher.domain.model.HomeListMode
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Spec 013 / RF-013-01 — Home scrubber rail: ★ + A–Z + # (tap + vertical drag).
 * Soft horizontal curve is best-effort (Niagara-like); remains usable if flat.
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
    var scrubberHeightPx by remember { mutableFloatStateOf(0f) }

    fun selectByIndex(index: Int) {
        val i = index.coerceIn(0, totalSlots - 1)
        if (i == 0) {
            onSelectFavorites()
        } else {
            onSelectLetter(letterKeys[i - 1])
        }
    }

    Column(
        modifier = modifier
            .width(44.dp)
            .fillMaxHeight()
            .padding(vertical = 8.dp)
            .onSizeChanged { scrubberHeightPx = it.height.toFloat() }
            .pointerInput(totalSlots, scrubberHeightPx) {
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                    if (scrubberHeightPx <= 0f) return@detectVerticalDragGestures
                    val fraction = (change.position.y / scrubberHeightPx).coerceIn(0f, 1f)
                    val index = (fraction * (totalSlots - 1)).roundToInt()
                    selectByIndex(index)
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScrubberGlyph(
            text = "★",
            selected = listMode is HomeListMode.Favorites,
            curveIndex = 0,
            totalSlots = totalSlots,
            onClick = onSelectFavorites,
            modifier = Modifier.weight(1f),
        )
        letterKeys.forEachIndexed { index, letter ->
            val selected = listMode is HomeListMode.Letter && listMode.letter == letter
            ScrubberGlyph(
                text = letter.toString(),
                selected = selected,
                curveIndex = index + 1,
                totalSlots = totalSlots,
                onClick = { onSelectLetter(letter) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ScrubberGlyph(
    text: String,
    selected: Boolean,
    curveIndex: Int,
    totalSlots: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fraction = if (totalSlots <= 1) 0.5f else curveIndex.toFloat() / (totalSlots - 1).toFloat()
    // Mild S-curve toward the list (negative X = left).
    val curvePx = (-sin(fraction * Math.PI) * 10.0).roundToInt()

    Box(
        modifier = modifier
            .offset { IntOffset(curvePx, 0) }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
