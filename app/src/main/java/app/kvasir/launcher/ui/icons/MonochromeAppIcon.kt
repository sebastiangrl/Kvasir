package app.kvasir.launcher.ui.icons

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.kvasir.launcher.data.apps.AppIconLoader
import app.kvasir.launcher.domain.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Spec 010 / RF-010-05 — provided from [app.kvasir.launcher.di.AppContainer] in MainActivity.
 */
val LocalAppIconLoader = staticCompositionLocalOf<AppIconLoader> {
    error("AppIconLoader not provided")
}

/**
 * Spec 010 / RF-010-03, RF-010-04, RF-010-06 —
 * Monochrome-tinted app icon; does not call LauncherApps / PackageManager.
 */
@Composable
fun MonochromeAppIcon(
    app: InstalledApp,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    loader: AppIconLoader = LocalAppIconLoader.current,
) {
    var image by remember(app.componentKey) { mutableStateOf<ImageBitmap?>(null) }
    val density = LocalDensity.current
    val px = with(density) { size.roundToPx() }.coerceAtLeast(1)

    LaunchedEffect(app.componentKey, px) {
        image = withContext(Dispatchers.Default) {
            val drawable = loader.loadDrawable(app) ?: return@withContext null
            drawable.toBoundedBitmap(px, px).asImageBitmap()
        }
    }

    val tint = MaterialTheme.colorScheme.onSurface
    Box(modifier = modifier.size(size)) {
        image?.let { bitmap ->
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.size(size),
                colorFilter = ColorFilter.tint(tint, BlendMode.SrcIn),
            )
        }
    }
}

/** Spec 010 / RF-010-06 — bounded decode; no full-res launcher bitmap kept in UI state. */
private fun Drawable.toBoundedBitmap(width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    mutate()
    setBounds(0, 0, width, height)
    draw(canvas)
    return bitmap
}
