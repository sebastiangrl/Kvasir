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
import app.kvasir.launcher.ui.home.HomeScreen
import app.kvasir.launcher.ui.home.HomeViewModel
import app.kvasir.launcher.ui.theme.KvasirTheme

/**
 * Spec 001 / RF-001-03, RF-001-04, RF-001-05, RF-001-09, RF-001-10 —
 * HOME Activity: singleTask warm path via onNewIntent; Back must not finish.
 * Composables do not call PackageManager / LauncherApps.
 */
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels {
        val app = application as KvasirApp
        HomeViewModel.factory(app.container.defaultHomeRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // RF-001-05 — consume Back / predictive back; stay on Home.
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(enabled = true) {
                override fun handleOnBackPressed() {
                    // Intentionally empty: do not finish() the HOME Activity.
                }
            },
        )

        setContent {
            val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
            KvasirTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        showDefaultHomeCta = uiState.showDefaultHomeCta,
                        onChooseHomeClick = homeViewModel::openHomePicker,
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
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
