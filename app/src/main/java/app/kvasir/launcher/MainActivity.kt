package app.kvasir.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.kvasir.launcher.ui.KvasirRoot
import app.kvasir.launcher.ui.home.HomeViewModel
import app.kvasir.launcher.ui.overlay.OverlayViewModel
import app.kvasir.launcher.ui.settings.SettingsViewModel
import app.kvasir.launcher.ui.theme.KvasirTheme

/**
 * Spec 001 + Spec 003 + Spec 004 / RF-004-07 —
 * HOME Activity hosts [KvasirRoot]; Composables do not call DataStore / LauncherApps.
 */
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels {
        val app = application as KvasirApp
        HomeViewModel.factory(
            defaultHomeRepository = app.container.defaultHomeRepository,
            launcherAppsRepository = app.container.launcherAppsRepository,
            preferencesRepository = app.container.preferencesRepository,
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        val app = application as KvasirApp
        SettingsViewModel.factory(
            launcherAppsRepository = app.container.launcherAppsRepository,
            preferencesRepository = app.container.preferencesRepository,
        )
    }

    private val overlayViewModel: OverlayViewModel by viewModels {
        val app = application as KvasirApp
        OverlayViewModel.factory(
            launcherAppsRepository = app.container.launcherAppsRepository,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // RF-001-05 — on Home, consume Back; stay on Home (do not finish).
        // Settings Back is handled by BackHandler inside KvasirRoot (RF-003-05).
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(enabled = true) {
                override fun handleOnBackPressed() {
                    // Intentionally empty: do not finish() the HOME Activity.
                }
            },
        )

        setContent {
            KvasirTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KvasirRoot(
                        homeViewModel = homeViewModel,
                        settingsViewModel = settingsViewModel,
                        overlayViewModel = overlayViewModel,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // RF-001-09 — re-read default-Home role when returning to foreground.
        homeViewModel.onResume()
    }

    override fun onNewIntent(intent: Intent) {
        // RF-001-04 — warm return to Home; do not recreate setContent.
        // RF-004-02 — leave overlay closed.
        super.onNewIntent(intent)
        setIntent(intent)
        overlayViewModel.close()
    }
}
