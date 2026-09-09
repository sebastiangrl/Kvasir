package app.kvasir.launcher.ui.home

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.kvasir.launcher.data.apps.AppDetailsNavigator
import app.kvasir.launcher.data.apps.AppShortcutsRepository
import app.kvasir.launcher.data.apps.LauncherAppsRepository
import app.kvasir.launcher.data.calendar.CalendarEventNavigator
import app.kvasir.launcher.data.calendar.CalendarEventsRepository
import app.kvasir.launcher.data.home.DefaultHomeRepository
import app.kvasir.launcher.data.notifications.NotificationBadgeRepository
import app.kvasir.launcher.data.pomodoro.PomodoroController
import app.kvasir.launcher.data.prefs.PreferencesRepository
import app.kvasir.launcher.data.system.SystemPanels
import app.kvasir.launcher.domain.AppLabelFilter
import app.kvasir.launcher.domain.FavoritesResolver
import app.kvasir.launcher.domain.HabitStreak
import app.kvasir.launcher.domain.PomodoroDailyLog
import app.kvasir.launcher.domain.model.AppShortcut
import app.kvasir.launcher.domain.model.Habit
import app.kvasir.launcher.domain.model.HomeListMode
import app.kvasir.launcher.domain.model.InstalledApp
import app.kvasir.launcher.domain.model.NextCalendarEvent
import app.kvasir.launcher.domain.model.PomodoroSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Spec 003 favorites + Spec 005 habits + Spec 008 + Spec 012 + Spec 013 +
 * Spec 014 / RF-014-04 + Spec 015 / RF-015-03, RF-015-06, RF-015-07 +
 * Spec 017 / RF-017-04, RF-017-05, RF-017-06 + Spec 018 / RF-018-02…05 +
 * Spec 021 / RF-021-01 —
 * Home UI state; Composables never call DataStore / LauncherApps / CalendarContract /
 * AlarmManager / NotificationListener / ShortcutManager.
 */

data class ShortcutsSheetState(
    val app: InstalledApp,
    val shortcuts: List<AppShortcut>,
)

data class HomeHabitRow(
    val habit: Habit,
    val completed: Boolean,
    /** Spec 014 — consecutive days; show in UI when ≥ 1. */
    val streak: Int = 0,
)

data class HomeUiState(
    val showDefaultHomeCta: Boolean = false,
    val searchQuery: String = "",
    val favorites: List<InstalledApp> = emptyList(),
    /** Favorites before search filter (for empty vs no-match copy). */
    val hasAnyFavorites: Boolean = false,
    val favoritesReady: Boolean = false,
    val habitRows: List<HomeHabitRow> = emptyList(),
    /** Spec 013 — ★ or letter; ephemeral. */
    val listMode: HomeListMode = HomeListMode.Favorites,
    /**
     * Spec 013 — apps for letter mode or global search (and ★ list when chrome).
     * Prefer this over [favorites] for the primary list once scrubber UI lands (T2).
     */
    val catalogApps: List<InstalledApp> = emptyList(),
    /** True only for ★ + blank query (clock / habits chrome). */
    val showingFavoritesChrome: Boolean = true,
    val isQueryActive: Boolean = false,
)

class HomeViewModel(
    application: Application,
    private val defaultHomeRepository: DefaultHomeRepository,
    private val launcherAppsRepository: LauncherAppsRepository,
    private val preferencesRepository: PreferencesRepository,
    private val calendarEventsRepository: CalendarEventsRepository,
    private val pomodoroController: PomodoroController,
    notificationBadgeRepository: NotificationBadgeRepository,
    private val appShortcutsRepository: AppShortcutsRepository,
) : AndroidViewModel(application) {

    private val defaultHomeCta = MutableStateFlow(false)
    /** Spec 008 / RF-008-05 — ephemeral; not persisted. */
    private val searchQuery = MutableStateFlow("")
    /** Spec 013 — ★ vs letter; ephemeral. */
    private val listMode = MutableStateFlow<HomeListMode>(HomeListMode.Favorites)

    /** Spec 012 — separate from uiState so clock tick / favorites do not share this refresh. */
    private val nextEventInternal = MutableStateFlow<NextCalendarEvent?>(null)
    val nextEvent: StateFlow<NextCalendarEvent?> = nextEventInternal.asStateFlow()

    /** Spec 015 / RF-015-03, RF-015-07 — session Flow isolated from clock 1 Hz. */
    val pomodoroSession: StateFlow<PomodoroSession> =
        preferencesRepository.pomodoroSession.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PomodoroSession.Idle,
        )

    /** Spec 021 / RF-021-01 — completed work sessions today; isolated from clock 1 Hz. */
    val pomodoroTodayCount: StateFlow<Int> =
        preferencesRepository.pomodoroDaily
            .map { daily -> PomodoroDailyLog.todayCount(daily, PomodoroDailyLog.dayKey()) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0,
            )

    /**
     * Spec 017 / RF-017-04, RF-017-06 — packages with badge; isolated from clock 1 Hz.
     * Empty when listener disabled / disconnected.
     */
    val badgedPackages: StateFlow<Set<String>> =
        notificationBadgeRepository.badgedPackages.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet(),
        )

    /** Spec 018 — non-null while shortcuts sheet is open. */
    private val shortcutsSheetInternal = MutableStateFlow<ShortcutsSheetState?>(null)
    val shortcutsSheet: StateFlow<ShortcutsSheetState?> = shortcutsSheetInternal.asStateFlow()

    /**
     * Spec 015 / RF-015-06 — Activity collects and requests POST_NOTIFICATIONS, then calls
     * [onNotificationPermissionSettled] (granted or denied; timer still starts).
     */
    private val notificationPermissionRequestsInternal = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val notificationPermissionRequests = notificationPermissionRequestsInternal.asSharedFlow()

    private data class HomeAppsBase(
        val showCta: Boolean,
        val resolvedFavorites: List<InstalledApp>,
        val allApps: List<InstalledApp>,
        val favoritesReady: Boolean,
        val habits: List<Habit>,
        val completedIds: Set<String>,
    )

    private data class HomeBase(
        val showCta: Boolean,
        val resolvedFavorites: List<InstalledApp>,
        val allApps: List<InstalledApp>,
        val favoritesReady: Boolean,
        val habitRows: List<HomeHabitRow>,
    )

    val uiState: StateFlow<HomeUiState> = combine(
        combine(
            combine(
                defaultHomeCta,
                launcherAppsRepository.snapshot,
                preferencesRepository.favoriteKeys,
                preferencesRepository.habits,
                preferencesRepository.habitDayState,
            ) { showCta, snapshot, favoriteKeys, habits, dayState ->
                val resolved = FavoritesResolver.resolve(
                    favoriteKeys = favoriteKeys,
                    installed = snapshot.apps,
                )
                HomeAppsBase(
                    showCta = showCta,
                    resolvedFavorites = resolved,
                    allApps = snapshot.apps,
                    favoritesReady = snapshot.appsLoaded,
                    habits = habits,
                    completedIds = dayState.completedIds,
                )
            },
            preferencesRepository.habitHistory,
        ) { appsBase, history ->
            val today = LocalDate.now().toEpochDay()
            HomeBase(
                showCta = appsBase.showCta,
                resolvedFavorites = appsBase.resolvedFavorites,
                allApps = appsBase.allApps,
                favoritesReady = appsBase.favoritesReady,
                habitRows = appsBase.habits.map { habit ->
                    val completed = habit.id in appsBase.completedIds
                    HomeHabitRow(
                        habit = habit,
                        completed = completed,
                        streak = HabitStreak.streakForHabit(
                            history = history,
                            habitId = habit.id,
                            todayCompleted = completed,
                            todayEpochDay = today,
                        ),
                    )
                },
            )
        },
        searchQuery,
        listMode,
    ) { base, query, mode ->
        val homeFilter = AppLabelFilter.filterHome(
            installed = base.allApps,
            favorites = base.resolvedFavorites,
            query = query,
            mode = mode,
        )
        val favoritesForLegacyUi = AppLabelFilter.filterByQuery(base.resolvedFavorites, query)
        HomeUiState(
            showDefaultHomeCta = base.showCta,
            searchQuery = query,
            favorites = favoritesForLegacyUi,
            hasAnyFavorites = base.resolvedFavorites.isNotEmpty(),
            favoritesReady = base.favoritesReady,
            habitRows = base.habitRows,
            listMode = mode,
            catalogApps = homeFilter.apps,
            showingFavoritesChrome = homeFilter.showingFavoritesChrome,
            isQueryActive = homeFilter.queryActive,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    /** RF-001-09 + Spec 014 / RF-014-02 — default-Home role + persist habit day roll. */
    fun onResume() {
        val isDefault = defaultHomeRepository.isDefaultHome()
        defaultHomeCta.update { !isDefault }
        refreshNextEvent()
        viewModelScope.launch {
            preferencesRepository.ensureHabitDayRolled()
        }
    }

    /**
     * Spec 012 / RF-012-02, RF-012-04, RF-012-06 —
     * Query only when READ_CALENDAR is granted; otherwise clear. Not driven by clock 1 Hz.
     */
    fun refreshNextEvent() {
        viewModelScope.launch {
            if (!calendarEventsRepository.hasReadPermission()) {
                nextEventInternal.value = null
                return@launch
            }
            nextEventInternal.value = calendarEventsRepository.nextEvent()
        }
    }

    /** RF-001-08 — open system Home picker / settings (reversible). */
    fun openHomePicker() {
        val intent = defaultHomeRepository.createHomePickerIntent()
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }

    /** RF-003-03 — launch via repository (safe for Home process). */
    fun launchApp(app: InstalledApp) {
        launcherAppsRepository.launch(app)
    }

    /** Spec 008 / RF-008-01 — ephemeral search query (013: non-blank → global catalog). */
    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    /** Spec 013 — scrubber letter; clears query so letter filter applies. */
    fun selectLetter(letter: Char) {
        searchQuery.value = ""
        listMode.value = HomeListMode.Letter(letter)
    }

    /** Spec 013 / RF-013-07 — ★ + empty query (Back / onNewIntent). */
    fun resetToFavorites() {
        searchQuery.value = ""
        listMode.value = HomeListMode.Favorites
    }

    /** Spec 009 / RF-009-01 — QS then notifications; no-op safe (outside Compose). */
    fun expandSystemPanel() {
        SystemPanels.expandPreferredPanel(getApplication())
    }

    /** Spec 009 / RF-009-04, RF-009-05 — system app details; does not mutate favorites. */
    fun openAppDetails(app: InstalledApp) {
        AppDetailsNavigator.open(getApplication(), app.packageName)
    }

    /**
     * Spec 018 / RF-018-01…04 + Spec 009 —
     * Long-press: load shortcuts; empty → details; else open sheet.
     */
    fun onAppLongClick(app: InstalledApp) {
        viewModelScope.launch(Dispatchers.Default) {
            val shortcuts = appShortcutsRepository.shortcutsFor(app.packageName)
            if (shortcuts.isEmpty()) {
                openAppDetails(app)
            } else {
                shortcutsSheetInternal.value = ShortcutsSheetState(app = app, shortcuts = shortcuts)
            }
        }
    }

    fun dismissShortcutsSheet() {
        shortcutsSheetInternal.value = null
    }

    fun onShortcutClick(shortcut: AppShortcut) {
        shortcutsSheetInternal.value = null
        appShortcutsRepository.startShortcut(shortcut)
    }

    fun onShortcutsSheetDetailsClick() {
        val app = shortcutsSheetInternal.value?.app ?: return
        shortcutsSheetInternal.value = null
        openAppDetails(app)
    }

    /** Spec 012 / RF-012-07 — open event / calendar app; no ContentResolver in UI. */
    fun openNextEvent() {
        val event = nextEventInternal.value ?: return
        CalendarEventNavigator.open(getApplication(), event)
    }

    /** RF-005-03 — persist habit completion immediately. */
    fun setHabitCompleted(habitId: String, completed: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHabitCompleted(habitId, completed)
        }
    }

    /** Spec 015 — ask notification permission (Activity), then start. */
    fun startPomodoro() {
        notificationPermissionRequestsInternal.tryEmit(Unit)
    }

    fun onNotificationPermissionSettled() {
        viewModelScope.launch {
            pomodoroController.start()
        }
    }

    fun pausePomodoro() {
        viewModelScope.launch {
            pomodoroController.pause()
        }
    }

    fun resumePomodoro() {
        viewModelScope.launch {
            pomodoroController.resume()
        }
    }

    fun stopPomodoro() {
        viewModelScope.launch {
            pomodoroController.stop()
        }
    }

    companion object {
        fun factory(
            defaultHomeRepository: DefaultHomeRepository,
            launcherAppsRepository: LauncherAppsRepository,
            preferencesRepository: PreferencesRepository,
            calendarEventsRepository: CalendarEventsRepository,
            pomodoroController: PomodoroController,
            notificationBadgeRepository: NotificationBadgeRepository,
            appShortcutsRepository: AppShortcutsRepository,
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as Application
                    HomeViewModel(
                        application = application,
                        defaultHomeRepository = defaultHomeRepository,
                        launcherAppsRepository = launcherAppsRepository,
                        preferencesRepository = preferencesRepository,
                        calendarEventsRepository = calendarEventsRepository,
                        pomodoroController = pomodoroController,
                        notificationBadgeRepository = notificationBadgeRepository,
                        appShortcutsRepository = appShortcutsRepository,
                    )
                }
            }
    }
}
