package app.kvasir.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kvasir.launcher.ui.home.HomeScreen
import app.kvasir.launcher.ui.home.HomeViewModel
import app.kvasir.launcher.ui.overlay.AppsOverlay
import app.kvasir.launcher.ui.overlay.OverlayViewModel
import app.kvasir.launcher.ui.settings.SettingsScreen
import app.kvasir.launcher.ui.settings.SettingsViewModel

/**
 * Spec 003 + Spec 004 / RF-004-01, RF-004-02, RF-004-08 —
 * Home | Settings; lazy overlay; Back: overlay > Settings > stay on Home.
 */
@Composable
fun KvasirRoot(
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
    overlayViewModel: OverlayViewModel,
    modifier: Modifier = Modifier,
) {
    var destination by rememberSaveable { mutableStateOf(RootDestination.Home) }
    val overlayState by overlayViewModel.uiState.collectAsStateWithLifecycle()

    // RF-003-05 — Settings Back → Home (only when overlay closed).
    BackHandler(enabled = destination == RootDestination.Settings && !overlayState.isOpen) {
        destination = RootDestination.Home
    }
    // RF-004-02 — Overlay Back → close; do not finish Activity.
    BackHandler(enabled = overlayState.isOpen) {
        overlayViewModel.close()
    }

    Box(modifier = modifier) {
        when (destination) {
            RootDestination.Home -> {
                val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
                HomeScreen(
                    showDefaultHomeCta = uiState.showDefaultHomeCta,
                    onChooseHomeClick = homeViewModel::openHomePicker,
                    favorites = uiState.favorites,
                    favoritesReady = uiState.favoritesReady,
                    onFavoriteClick = homeViewModel::launchApp,
                    habitRows = uiState.habitRows,
                    onHabitCheckedChange = homeViewModel::setHabitCompleted,
                    onSettingsClick = {
                        settingsViewModel.onSettingsOpened()
                        destination = RootDestination.Settings
                    },
                    onOpenOverlay = overlayViewModel::open,
                )
            }
            RootDestination.Settings -> {
                val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
                SettingsScreen(
                    rows = uiState.rows,
                    appsLoaded = uiState.appsLoaded,
                    onFavoriteChange = settingsViewModel::setFavorite,
                    habits = uiState.habits,
                    onAddHabit = settingsViewModel::addHabit,
                    onRemoveHabit = settingsViewModel::removeHabit,
                    onRenameHabit = settingsViewModel::renameHabit,
                    onBack = { destination = RootDestination.Home },
                )
            }
        }

        // RF-004-08 — lazy: only compose when open.
        if (overlayState.isOpen) {
            AppsOverlay(
                selectedLetter = overlayState.selectedLetter,
                filteredApps = overlayState.filteredApps,
                appsLoaded = overlayState.appsLoaded,
                onSelectLetter = overlayViewModel::selectLetter,
                onAppClick = overlayViewModel::launchAndClose,
                onClose = overlayViewModel::close,
            )
        }
    }
}
