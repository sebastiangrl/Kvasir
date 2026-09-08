package app.kvasir.launcher.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kvasir.launcher.R
import app.kvasir.launcher.domain.LetterBucket
import app.kvasir.launcher.domain.model.InstalledApp
import app.kvasir.launcher.ui.gesture.onVerticalSwipe
import kotlin.math.roundToInt

/**
 * Spec 004 / RF-004-02…05, RF-004-07 —
 * Full-screen apps overlay: list + scrubber; swipe-down closes.
 */
@Composable
fun AppsOverlay(
    selectedLetter: Char,
    filteredApps: List<InstalledApp>,
    appsLoaded: Boolean,
    onSelectLetter: (Char) -> Unit,
    onAppClick: (InstalledApp) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .onVerticalSwipe(onSwipeDown = onClose),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = 16.dp),
            ) {
                when {
                    !appsLoaded -> {
                        // No fake rows while loading.
                    }
                    filteredApps.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.overlay_letter_empty),
                            modifier = Modifier.padding(horizontal = 24.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(
                                items = filteredApps,
                                key = { it.componentKey },
                            ) { app ->
                                Text(
                                    text = app.label,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onAppClick(app) }
                                        .padding(horizontal = 24.dp, vertical = 12.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }

            LetterScrubber(
                selectedLetter = selectedLetter,
                onSelectLetter = onSelectLetter,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(40.dp)
                    .padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun LetterScrubber(
    selectedLetter: Char,
    onSelectLetter: (Char) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keys = LetterBucket.KEYS
    var scrubberHeightPx by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .onSizeChanged { scrubberHeightPx = it.height.toFloat() }
            .pointerInput(keys, scrubberHeightPx) {
                detectVerticalDragGestures { change, _ ->
                    change.consume()
                    if (scrubberHeightPx <= 0f) return@detectVerticalDragGestures
                    val fraction = (change.position.y / scrubberHeightPx).coerceIn(0f, 1f)
                    val index = (fraction * (keys.lastIndex)).roundToInt()
                        .coerceIn(0, keys.lastIndex)
                    onSelectLetter(keys[index])
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        keys.forEach { letter ->
            val selected = letter == selectedLetter
            Text(
                text = letter.toString(),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectLetter(letter) }
                    .padding(horizontal = 4.dp),
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
}
