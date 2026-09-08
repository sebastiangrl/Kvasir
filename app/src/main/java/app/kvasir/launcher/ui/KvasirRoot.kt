package app.kvasir.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kvasir.launcher.ui.home.HomeScreen
import app.kvasir.launcher.ui.home.HomeViewModel
import app.kvasir.launcher.ui.settings.SettingsScreen
import app.kvasir.launcher.ui.settings.SettingsViewModel

/**
 * Spec 003 / RF-003-05, RF-003-07, RF-003-08 —
 * Sealed Home | Settings root; Back from Settings returns Home without finish().
 */
@Composable
fun KvasirRoot(
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    var destination by rememberSaveable { mutableStateOf(RootDestination.Home) }

    // RF-003-05 — Back on Settings → Home; do not finish the HOME Activity.
    BackHandler(enabled = destination == RootDestination.Settings) {
        destination = RootDestination.Home
    }

    when (destination) {
        RootDestination.Home -> {
            val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
            HomeScreen(
                showDefaultHomeCta = uiState.showDefaultHomeCta,
                onChooseHomeClick = homeViewModel::openHomePicker,
                favorites = uiState.favorites,
                favoritesReady = uiState.favoritesReady,
                onFavoriteClick = homeViewModel::launchApp,
                onSettingsClick = {
                    settingsViewModel.onSettingsOpened()
                    destination = RootDestination.Settings
                },
                modifier = modifier,
            )
        }
        RootDestination.Settings -> {
            val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
            SettingsScreen(
                rows = uiState.rows,
                appsLoaded = uiState.appsLoaded,
                onFavoriteChange = settingsViewModel::setFavorite,
                onBack = { destination = RootDestination.Home },
                modifier = modifier,
            )
        }
    }
}
