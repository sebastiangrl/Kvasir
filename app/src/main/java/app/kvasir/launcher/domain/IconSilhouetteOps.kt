package app.kvasir.launcher.domain

/**
 * Spec 019 / RF-019-02 + Spec 020 / RF-020-04 —
 * Pixel ops for monochrome silhouettes (JVM-testable).
 * Android rasterization lives in data/apps; Compose only tints the result.
 */
object IconSilhouetteOps {
    const val ALPHA_VISIBLE = 24
    /** Spec 020 — draw at this multiple of display size, then scale down. */
    const val SUPER_SAMPLE = 3

    data class Bounds(
        val left: Int,
        val top: Int,
        val right: Int,
        val bottom: Int,
    ) {
        val width: Int get() = right - left + 1
        val height: Int get() = bottom - top + 1
    }

    fun visibleAlphaBounds(
        pixels: IntArray,
        width: Int,
        height: Int,
        alphaThreshold: Int = ALPHA_VISIBLE,
    ): Bounds? {
        if (width <= 0 || height <= 0 || pixels.size < width * height) return null
        var minX = width
        var minY = height
        var maxX = -1
        var maxY = -1
        for (y in 0 until height) {
            val row = y * width
            for (x in 0 until width) {
                val alpha = pixels[row + x] ushr 24
                if (alpha >= alphaThreshold) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }
        if (maxX < minX) return null
        return Bounds(left = minX, top = minY, right = maxX, bottom = maxY)
    }

    fun padBounds(bounds: Bounds, width: Int, height: Int, pad: Int = 1): Bounds =
        Bounds(
            left = (bounds.left - pad).coerceAtLeast(0),
            top = (bounds.top - pad).coerceAtLeast(0),
            right = (bounds.right + pad).coerceAtMost(width - 1),
            bottom = (bounds.bottom + pad).coerceAtMost(height - 1),
        )

    /**
     * Spec 020 / RF-020-04 — ARGB → white with coverage alpha from luma (dark or bright ink).
     * In-place; returns [pixels] for chaining.
     */
    fun applyLuminanceMaskInPlace(pixels: IntArray): IntArray {
        for (i in pixels.indices) {
            val c = pixels[i]
            val a = c ushr 24
            if (a == 0) {
                pixels[i] = 0
                continue
            }
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            val luma = luminance(r, g, b)
            // Dark strokes and light strokes both keep coverage.
            val ink = maxOf(luma, 255 - luma)
            val maskAlpha = (a * ink) / 255
            pixels[i] = if (maskAlpha == 0) {
                0
            } else {
                (maskAlpha shl 24) or 0x00FFFFFF
            }
        }
        return pixels
    }

    /** Rec. 601 luma, 0..255. */
    fun luminance(r: Int, g: Int, b: Int): Int =
        ((r * 299) + (g * 587) + (b * 114)) / 1000
}
