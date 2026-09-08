package app.kvasir.launcher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Spec 019 / RF-019-02 — visible-alpha crop bounds for icon silhouettes.
 */
class IconSilhouetteOpsTest {

    @Test
    fun visibleAlphaBounds_empty_returnsNull() {
        val pixels = IntArray(16)
        assertNull(IconSilhouetteOps.visibleAlphaBounds(pixels, width = 4, height = 4))
    }

    @Test
    fun visibleAlphaBounds_findsOpaqueRect() {
        // 4x4; opaque 2x2 at (1,1)-(2,2)
        val pixels = IntArray(16)
        val opaque = 0xFF00FF00.toInt()
        pixels[1 + 1 * 4] = opaque
        pixels[2 + 1 * 4] = opaque
        pixels[1 + 2 * 4] = opaque
        pixels[2 + 2 * 4] = opaque
        val bounds = IconSilhouetteOps.visibleAlphaBounds(pixels, width = 4, height = 4)
        assertEquals(IconSilhouetteOps.Bounds(1, 1, 2, 2), bounds)
    }

    @Test
    fun padBounds_clampsToCanvas() {
        val padded = IconSilhouetteOps.padBounds(
            bounds = IconSilhouetteOps.Bounds(0, 1, 2, 3),
            width = 4,
            height = 4,
            pad = 1,
        )
        assertEquals(IconSilhouetteOps.Bounds(0, 0, 3, 3), padded)
    }
}
