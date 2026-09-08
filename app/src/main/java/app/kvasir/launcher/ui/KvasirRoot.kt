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
import app.kvasir.launcher.domain.model.HomeListMode
import app.kvasir.launcher.domain.model.ThemeMode
import app.kvasir.launcher.ui.home.HomeScreen
import app.kvasir.launcher.ui.home.HomeViewModel
import app.kvasir.launcher.ui.settings.SettingsScreen
import app.kvasir.launcher.ui.settings.SettingsViewModel

/**
 * Spec 003 + Spec 008 + Spec 009 + Spec 012 + Spec 013 —
 * Home (Niagara scrubber) | Settings. Overlay A–Z removed (RF-013-05).
 */
@Composable
fun KvasirRoot(
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    var destination by rememberSaveable { mutableStateOf(RootDestination.Home) }

    // RF-003-05 — Settings Back → Home.
    BackHandler(enabled = destination == RootDestination.Settings) {
        destination = RootDestination.Home
    }

    Box(modifier = modifier) {
        when (destination) {
            RootDestination.Home -> {
                val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
                val nextEvent by homeViewModel.nextEvent.collectAsStateWithLifecycle()
                val leaveFavoritesChrome =
                    uiState.isQueryActive || uiState.listMode !is HomeListMode.Favorites
                // Spec 013 / RF-013-07 — Back from letter/query → ★ (do not finish Activity).
                BackHandler(enabled = leaveFavoritesChrome) {
                    homeViewModel.resetToFavorites()
                }
                HomeScreen(
                    showDefaultHomeCta = uiState.showDefaultHomeCta,
                    onChooseHomeClick = homeViewModel::openHomePicker,
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = homeViewModel::setSearchQuery,
                    favorites = uiState.favorites,
                    hasAnyFavorites = uiState.hasAnyFavorites,
                    favoritesReady = uiState.favoritesReady,
                    catalogApps = uiState.catalogApps,
                    showingFavoritesChrome = uiState.showingFavoritesChrome,
                    isQueryActive = uiState.isQueryActive,
                    listMode = uiState.listMode,
                    onFavoriteClick = homeViewModel::launchApp,
                    onFavoriteLongClick = homeViewModel::openAppDetails,
                    onCatalogAppClick = homeViewModel::launchApp,
                    onCatalogAppLongClick = homeViewModel::openAppDetails,
                    habitRows = uiState.habitRows,
                    onHabitCheckedChange = homeViewModel::setHabitCompleted,
                    onSettingsClick = {
                        settingsViewModel.onSettingsOpened()
                        destination = RootDestination.Settings
                    },
                    onExpandSystemPanel = homeViewModel::expandSystemPanel,
                    onSelectFavorites = homeViewModel::resetToFavorites,
                    onSelectLetter = homeViewModel::selectLetter,
                    nextEvent = nextEvent,
                    onNextEventClick = homeViewModel::openNextEvent,
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
                    darkTheme = uiState.themeMode == ThemeMode.Dark,
                    onDarkThemeChange = settingsViewModel::setDarkTheme,
                    onBack = { destination = RootDestination.Home },
                )
            }
        }
    }
}
