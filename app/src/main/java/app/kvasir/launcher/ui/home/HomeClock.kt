package app.kvasir.launcher.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kvasir.launcher.ui.theme.AnuratiFontFamily
import kotlinx.coroutines.delay
import java.time.Instant
import java.util.Locale

/**
 * Spec 001 / RF-001-06 + Spec 011 + Spec 019 / RF-019-06 +
 * Spec 020 / RF-020-01, RF-020-02 —
 * Isolated 1 Hz clock: Anurati day on optical center; time left; date right; event below.
 * [eventLine] is preformatted (012); not driven by the 1 Hz tick.
 */
@Composable
fun HomeClock(
    modifier: Modifier = Modifier,
    eventLine: String = "",
    onEventClick: () -> Unit = {},
) {
    var now by remember { mutableStateOf(Instant.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = Instant.now()
            delay(1_000L)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Spec 020 / RF-020-02 — day absolute center; time/date flank without weight push.
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = ClockFormat.formatWeekdayAbbreviated(now),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = AnuratiFontFamily,
                    fontSize = 72.sp,
                    lineHeight = 76.sp,
                    letterSpacing = 4.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = ClockFormat.formatTime(now),
                modifier = Modifier.align(Alignment.CenterStart),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = ClockFormat.formatDate(now),
                modifier = Modifier.align(Alignment.CenterEnd),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (eventLine.isNotEmpty()) {
            Text(
                text = eventLine.uppercase(Locale.getDefault()),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable(onClick = onEventClick),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
