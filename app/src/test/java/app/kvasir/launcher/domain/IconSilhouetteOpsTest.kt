package app.kvasir.launcher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Spec 019 / RF-019-02 + Spec 020 / RF-020-04 — silhouette crop + luminance mask.
 */
class IconSilhouetteOpsTest {

    @Test
    fun visibleAlphaBounds_empty_returnsNull() {
        val pixels = IntArray(16)
        assertNull(IconSilhouetteOps.visibleAlphaBounds(pixels, width = 4, height = 4))
    }

    @Test
    fun visibleAlphaBounds_findsOpaqueRect() {
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

    @Test
    fun luminance_whiteIsMax_blackIsZero() {
        assertEquals(255, IconSilhouetteOps.luminance(255, 255, 255))
        assertEquals(0, IconSilhouetteOps.luminance(0, 0, 0))
    }

    @Test
    fun applyLuminanceMask_darkInkBecomesWhiteWithAlpha() {
        // Opaque black stroke → full coverage white mask.
        val pixels = intArrayOf(0xFF000000.toInt())
        IconSilhouetteOps.applyLuminanceMaskInPlace(pixels)
        val a = pixels[0] ushr 24
        val rgb = pixels[0] and 0x00FFFFFF
        assertEquals(255, a)
        assertEquals(0x00FFFFFF, rgb)
    }

    @Test
    fun applyLuminanceMask_brightInkAlsoKeepsCoverage() {
        val pixels = intArrayOf(0xFFFFFFFF.toInt())
        IconSilhouetteOps.applyLuminanceMaskInPlace(pixels)
        assertEquals(255, pixels[0] ushr 24)
        assertEquals(0x00FFFFFF, pixels[0] and 0x00FFFFFF)
    }

    @Test
    fun applyLuminanceMask_transparentStaysZero() {
        val pixels = intArrayOf(0x00FFFFFF)
        IconSilhouetteOps.applyLuminanceMaskInPlace(pixels)
        assertEquals(0, pixels[0])
    }

    @Test
    fun applyLuminanceMask_midGrayHasCoverage() {
        val pixels = intArrayOf(0xFF808080.toInt())
        IconSilhouetteOps.applyLuminanceMaskInPlace(pixels)
        val a = pixels[0] ushr 24
        assertTrue("expected mid coverage, got $a", a in 100..200)
    }

    @Test
    fun superSampleFactor_isThree() {
        assertEquals(3, IconSilhouetteOps.SUPER_SAMPLE)
    }
}
