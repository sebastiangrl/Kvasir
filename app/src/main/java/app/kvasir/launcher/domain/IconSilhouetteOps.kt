package app.kvasir.launcher.domain

/**
 * Spec 019 / RF-019-02 — pixel ops for monochrome silhouettes (JVM-testable).
 * Android rasterization lives in data/apps; Compose only tints the result.
 */
object IconSilhouetteOps {
    const val ALPHA_VISIBLE = 24

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
}
