package app.kvasir.launcher.data.apps

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.ScaleDrawable
import app.kvasir.launcher.domain.IconSilhouetteOps

/**
 * Spec 019 / RF-019-02 —
 * Rasterize a tintable glyph: adaptive **foreground** only (skip the circular plate).
 */
object IconSilhouette {

    fun toBoundedBitmap(drawable: Drawable, sizePx: Int): Bitmap {
        val size = sizePx.coerceAtLeast(1)
        val glyph = drawable.glyphLayer()
        val raster = glyph.rasterize(size)
        val cropped = cropToVisibleAlpha(raster)
        if (cropped === raster) return raster
        raster.recycle()
        if (cropped.width == size && cropped.height == size) return cropped
        val scaled = Bitmap.createScaledBitmap(cropped, size, size, true)
        if (scaled !== cropped) cropped.recycle()
        return scaled
    }

    private fun Drawable.glyphLayer(): Drawable {
        var current: Drawable = this
        repeat(8) {
            current = when (current) {
                is AdaptiveIconDrawable -> current.foreground ?: return current
                is InsetDrawable -> current.drawable ?: return current
                is ScaleDrawable -> current.drawable ?: return current
                is DrawableWrapper -> current.drawable ?: return current
                else -> return current
            }
        }
        return current
    }

    private fun Drawable.rasterize(size: Int): Bitmap {
        val copy = constantState?.newDrawable()?.mutate() ?: mutate()
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        copy.setBounds(0, 0, size, size)
        copy.draw(canvas)
        return bitmap
    }

    private fun cropToVisibleAlpha(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val raw = IconSilhouetteOps.visibleAlphaBounds(pixels, width, height) ?: return bitmap
        val bounds = IconSilhouetteOps.padBounds(raw, width, height)
        if (bounds.left == 0 && bounds.top == 0 &&
            bounds.right == width - 1 && bounds.bottom == height - 1
        ) {
            return bitmap
        }
        return Bitmap.createBitmap(
            bitmap,
            bounds.left,
            bounds.top,
            bounds.width,
            bounds.height,
        )
    }
}
