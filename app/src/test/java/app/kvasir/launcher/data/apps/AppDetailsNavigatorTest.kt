package app.kvasir.launcher.data.apps

import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Spec 009 / RF-009-04 — JVM tests for application-details targeting (no Uri mocks).
 */
class AppDetailsNavigatorTest {

    @Test
    fun packageDetailsUriString_usesPackageScheme() {
        assertEquals(
            "package:com.example.app",
            AppDetailsNavigator.packageDetailsUriString("com.example.app"),
        )
    }

    @Test
    fun detailsAction_isApplicationDetailsSettings() {
        assertEquals(
            "android.settings.APPLICATION_DETAILS_SETTINGS",
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        )
    }
}
