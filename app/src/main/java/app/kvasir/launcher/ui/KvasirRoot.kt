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
 * Spec 003 + Spec 008 + Spec 009 + Spec 012 + Spec 013 + Spec 015 + Spec 016 +
 * Spec 018 + Spec 019 —
 * Home (Niagara scrubber) | Settings hub. Overlay A–Z removed (RF-013-05).
 */
@Composable
fun KvasirRoot(
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    var destination by rememberSaveable { mutableStateOf(RootDestination.Home) }

    // Spec 016 / RF-016-02 — section → hub → Home.
    BackHandler(enabled = destination == RootDestination.Settings) {
        if (!settingsViewModel.navigateBackWithinSettings()) {
            destination = RootDestination.Home
        }
    }

    Box(modifier = modifier) {
        when (destination) {
            RootDestination.Home -> {
                val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
                val nextEvent by homeViewModel.nextEvent.collectAsStateWithLifecycle()
                val pomodoroSession by homeViewModel.pomodoroSession.collectAsStateWithLifecycle()
                val pomodoroTodayCount by homeViewModel.pomodoroTodayCount.collectAsStateWithLifecycle()
                val badgedPackages by homeViewModel.badgedPackages.collectAsStateWithLifecycle()
                val shortcutsSheet by homeViewModel.shortcutsSheet.collectAsStateWithLifecycle()
                val leaveFavoritesChrome =
                    uiState.isQueryActive || uiState.listMode !is HomeListMode.Favorites
                // Spec 018 — dismiss shortcuts sheet before scrubber Back.
                BackHandler(enabled = shortcutsSheet != null) {
                    homeViewModel.dismissShortcutsSheet()
                }
                // Spec 013 / RF-013-07 — Back from letter/query → ★ (do not finish Activity).
                BackHandler(enabled = shortcutsSheet == null && leaveFavoritesChrome) {
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
                    onFavoriteLongClick = homeViewModel::onAppLongClick,
                    onCatalogAppClick = homeViewModel::launchApp,
                    onCatalogAppLongClick = homeViewModel::onAppLongClick,
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
                    pomodoroSession = pomodoroSession,
                    pomodoroTodayCount = pomodoroTodayCount,
                    onPomodoroStart = homeViewModel::startPomodoro,
                    onPomodoroPause = homeViewModel::pausePomodoro,
                    onPomodoroResume = homeViewModel::resumePomodoro,
                    onPomodoroStop = homeViewModel::stopPomodoro,
                    badgedPackages = badgedPackages,
                    shortcutsSheet = shortcutsSheet,
                    onDismissShortcutsSheet = homeViewModel::dismissShortcutsSheet,
                    onShortcutClick = homeViewModel::onShortcutClick,
                    onShortcutsSheetDetailsClick = homeViewModel::onShortcutsSheetDetailsClick,
                )
            }
            RootDestination.Settings -> {
                val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
                val section by settingsViewModel.openSection.collectAsStateWithLifecycle()
                val permissionsStatus by settingsViewModel.permissionsStatus.collectAsStateWithLifecycle()
                SettingsScreen(
                    section = section,
                    onOpenSection = settingsViewModel::openSection,
                    rows = uiState.rows,
                    appsLoaded = uiState.appsLoaded,
                    onFavoriteChange = settingsViewModel::setFavorite,
                    habitRows = uiState.habitRows,
                    onAddHabit = settingsViewModel::addHabit,
                    onRemoveHabit = settingsViewModel::removeHabit,
                    onRenameHabit = settingsViewModel::renameHabit,
                    darkTheme = uiState.themeMode == ThemeMode.Dark,
                    onDarkThemeChange = settingsViewModel::setDarkTheme,
                    pomodoroConfig = uiState.pomodoroConfig,
                    onSavePomodoroConfig = settingsViewModel::setPomodoroConfig,
                    pomodoroHistory = uiState.pomodoroHistory,
                    permissionsStatus = permissionsStatus,
                    onOpenHomePicker = settingsViewModel::openHomePicker,
                    onRequestCalendarPermission = settingsViewModel::requestCalendarPermission,
                    onRequestNotificationsPermission = settingsViewModel::requestNotificationsPermission,
                    onOpenAppDetailsSettings = settingsViewModel::openAppDetailsSettings,
                    onOpenNotificationSettings = settingsViewModel::openNotificationSettings,
                    onOpenExactAlarmSettings = settingsViewModel::openExactAlarmSettings,
                    onOpenNotificationListenerSettings = settingsViewModel::openNotificationListenerSettings,
                    onBack = {
                        if (!settingsViewModel.navigateBackWithinSettings()) {
                            destination = RootDestination.Home
                        }
                    },
                )
            }
        }
    }
}
