package app.kvasir.launcher.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Spec 006 / RF-006-01, RF-006-06 — JVM parse/default tests.
 */
class ThemeModeTest {

    @Test
    fun fromStorage_lightAndDark() {
        assertEquals(ThemeMode.Light, ThemeMode.fromStorage("light"))
        assertEquals(ThemeMode.Dark, ThemeMode.fromStorage("dark"))
    }

    @Test
    fun fromStorage_nullOrInvalid_defaultsToLight() {
        assertEquals(ThemeMode.Light, ThemeMode.fromStorage(null))
        assertEquals(ThemeMode.Light, ThemeMode.fromStorage(""))
        assertEquals(ThemeMode.Light, ThemeMode.fromStorage("system"))
        assertEquals(ThemeMode.Light, ThemeMode.fromStorage("DARK"))
    }

    @Test
    fun storageValue_roundTrip() {
        assertEquals(ThemeMode.Light, ThemeMode.fromStorage(ThemeMode.Light.storageValue))
        assertEquals(ThemeMode.Dark, ThemeMode.fromStorage(ThemeMode.Dark.storageValue))
    }
}
