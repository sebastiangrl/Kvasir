package app.kvasir.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kvasir.launcher.domain.model.ThemeMode
import app.kvasir.launcher.ui.KvasirRoot
import app.kvasir.launcher.ui.home.HomeViewModel
import app.kvasir.launcher.ui.overlay.OverlayViewModel
import app.kvasir.launcher.ui.settings.SettingsViewModel
import app.kvasir.launcher.ui.theme.KvasirTheme

/**
 * Spec 001–006 — HOME Activity hosts [KvasirRoot]; theme via Compose only (RF-006-04).
 * Composables do not call DataStore / LauncherApps.
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
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(enabled = true) {
                override fun handleOnBackPressed() {
                    // Intentionally empty: do not finish() the HOME Activity.
                }
            },
        )

        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            // RF-006-02, RF-006-04 — recompose MaterialTheme; never setDefaultNightMode.
            KvasirTheme(darkTheme = themeMode == ThemeMode.Dark) {
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
        homeViewModel.onResume()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        overlayViewModel.close()
    }
}
