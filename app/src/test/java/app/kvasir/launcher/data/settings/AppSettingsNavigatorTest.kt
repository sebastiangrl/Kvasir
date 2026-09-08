package app.kvasir.launcher.data.settings

import android.provider.Settings
import app.kvasir.launcher.data.apps.AppDetailsNavigator
import app.kvasir.launcher.domain.model.SettingsSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Spec 016 / RF-016-01, RF-016-04 — section enum + settings intent builders (JVM).
 */
class AppSettingsNavigatorTest {

    @Test
    fun settingsSection_hasExpectedEntries() {
        assertEquals(5, SettingsSection.entries.size)
        assertTrue(SettingsSection.Permissions in SettingsSection.entries)
    }

    @Test
    fun packageUriString_matchesAppDetails() {
        assertEquals(
            AppDetailsNavigator.packageDetailsUriString("app.kvasir.launcher"),
            AppSettingsNavigator.packageUriString("app.kvasir.launcher"),
        )
    }

    @Test
    fun appNotificationSettings_usesSystemActionAndPackageExtraConstants() {
        assertEquals(
            "android.settings.APP_NOTIFICATION_SETTINGS",
            Settings.ACTION_APP_NOTIFICATION_SETTINGS,
        )
        assertEquals(
            "android.provider.extra.APP_PACKAGE",
            Settings.EXTRA_APP_PACKAGE,
        )
    }

    @Test
    fun exactAlarmSettings_usesRequestScheduleExactAlarmActionConstant() {
        assertEquals(
            "android.settings.REQUEST_SCHEDULE_EXACT_ALARM",
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
        )
    }
}
