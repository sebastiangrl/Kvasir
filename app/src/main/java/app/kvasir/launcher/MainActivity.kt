package app.kvasir.launcher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kvasir.launcher.domain.model.ThemeMode
import app.kvasir.launcher.ui.KvasirRoot
import app.kvasir.launcher.ui.home.HomeViewModel
import app.kvasir.launcher.ui.icons.LocalAppIconLoader
import app.kvasir.launcher.ui.settings.SettingsViewModel
import app.kvasir.launcher.ui.theme.KvasirTheme

/**
 * Spec 001–006 + Spec 010 + Spec 012 + Spec 013 / RF-013-05, RF-013-07 —
 * HOME Activity hosts [KvasirRoot]; theme via Compose only.
 * Composables do not call DataStore / LauncherApps / CalendarContract.
 */
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels {
        val app = application as KvasirApp
        HomeViewModel.factory(
            defaultHomeRepository = app.container.defaultHomeRepository,
            launcherAppsRepository = app.container.launcherAppsRepository,
            preferencesRepository = app.container.preferencesRepository,
            calendarEventsRepository = app.container.calendarEventsRepository,
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        val app = application as KvasirApp
        SettingsViewModel.factory(
            launcherAppsRepository = app.container.launcherAppsRepository,
            preferencesRepository = app.container.preferencesRepository,
        )
    }

    /** Spec 012 — ask READ_CALENDAR at most once per process (no Home CTA). */
    private var calendarPermissionRequestedThisProcess = false

    private val requestCalendarPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        homeViewModel.refreshNextEvent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // RF-001-05 — on Home, consume Back; stay on Home (do not finish).
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(enabled = true) {
                override fun handleOnBackPressed() {
                    // Intentionally empty: do not finish() the HOME Activity.
                }
            },
        )

        setContent {
            val app = application as KvasirApp
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalAppIconLoader provides app.container.appIconLoader,
            ) {
                KvasirTheme(darkTheme = themeMode == ThemeMode.Dark) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        KvasirRoot(
                            homeViewModel = homeViewModel,
                            settingsViewModel = settingsViewModel,
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.onResume()
        maybeRequestCalendarPermissionOnce()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Spec 013 / RF-013-07 — warm Home returns to ★.
        homeViewModel.resetToFavorites()
    }

    /** Spec 012 / RF-012-02 — system dialog once per process if not already granted. */
    private fun maybeRequestCalendarPermissionOnce() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CALENDAR,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return
        if (calendarPermissionRequestedThisProcess) return
        calendarPermissionRequestedThisProcess = true
        requestCalendarPermission.launch(Manifest.permission.READ_CALENDAR)
    }
}
