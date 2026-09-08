package app.kvasir.launcher.data.apps

import app.kvasir.launcher.domain.model.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * Spec 002 / RF-002-01, RF-002-04, RF-002-05 — JVM tests for filter + sort.
 */
class InstalledAppMapperTest {

    private val selfPackage = "app.kvasir.launcher"

    private val sample = listOf(
        InstalledApp(selfPackage, "MainActivity", "Kvasir"),
        InstalledApp("com.example.z", "ZActivity", "zebra"),
        InstalledApp("com.example.a", "AActivity", "Alpha"),
        InstalledApp("com.example.m", "MActivity", "mango"),
    )

    @Test
    fun prepareInstalledApps_excludesSelfPackage() {
        val result = InstalledAppMapper.prepareInstalledApps(
            apps = sample,
            selfPackageName = selfPackage,
            locale = Locale.US,
        )
        assertFalse(result.any { it.packageName == selfPackage })
        assertEquals(3, result.size)
    }

    @Test
    fun prepareInstalledApps_sortsByLabelCaseInsensitive() {
        val result = InstalledAppMapper.prepareInstalledApps(
            apps = sample,
            selfPackageName = selfPackage,
            locale = Locale.US,
        )
        assertEquals(listOf("Alpha", "mango", "zebra"), result.map { it.label })
    }

    @Test
    fun installedApp_componentKey_isStable() {
        val app = InstalledApp("com.example", "com.example.Main", "Example")
        assertEquals("com.example/com.example.Main", app.componentKey)
        assertTrue(app.label.isNotEmpty())
    }
}
