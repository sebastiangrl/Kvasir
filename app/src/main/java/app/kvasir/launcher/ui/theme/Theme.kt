package app.kvasir.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Spec 006 / RF-006-02, RF-006-04 + Spec 019 / RF-019-05 —
 * Material 3 from persisted mode; never setDefaultNightMode / recreate Activity.
 */
@Composable
fun KvasirTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

/**
 * Spec 019 / RF-019-05 — subtle vertical wash; no wallpaper bitmap.
 */
fun kvasirScreenGradient(darkTheme: Boolean): Brush {
    val colors = if (darkTheme) {
        listOf(Color(0xFF2A2730), Color(0xFF0C0B0E))
    } else {
        listOf(Color(0xFFF7F4FA), Color(0xFFE4DFE8))
    }
    return Brush.verticalGradient(colors = colors)
}
