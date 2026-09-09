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
 * Spec 019 / RF-019-02 + Spec 020 / RF-020-04 —
 * Adaptive **foreground** only; supersample + luminance mask → crisp tintable glyph.
 * Temporary hi-res bitmap is not kept in the Drawable LRU.
 */
object IconSilhouette {

    fun toBoundedBitmap(drawable: Drawable, sizePx: Int): Bitmap {
        val size = sizePx.coerceAtLeast(1)
        val hi = (size * IconSilhouetteOps.SUPER_SAMPLE).coerceAtLeast(size)
        val glyph = drawable.glyphLayer()
        val raster = glyph.rasterize(hi)
        val width = raster.width
        val height = raster.height
        val pixels = IntArray(width * height)
        raster.getPixels(pixels, 0, width, 0, 0, width, height)
        IconSilhouetteOps.applyLuminanceMaskInPlace(pixels)

        val cropped = cropMasked(pixels, width, height) ?: run {
            raster.recycle()
            return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        }
        raster.recycle()

        if (cropped.width == size && cropped.height == size) {
            return cropped
        }
        val scaled = Bitmap.createScaledBitmap(cropped, size, size, /* filter */ true)
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

    private fun cropMasked(pixels: IntArray, width: Int, height: Int): Bitmap? {
        val raw = IconSilhouetteOps.visibleAlphaBounds(pixels, width, height) ?: return null
        val bounds = IconSilhouetteOps.padBounds(raw, width, height)
        val outW = bounds.width
        val outH = bounds.height
        val out = IntArray(outW * outH)
        for (y in 0 until outH) {
            val srcRow = (bounds.top + y) * width + bounds.left
            val dstRow = y * outW
            System.arraycopy(pixels, srcRow, out, dstRow, outW)
        }
        val bitmap = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(out, 0, outW, 0, 0, outW, outH)
        return bitmap
    }
}
