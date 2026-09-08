package app.kvasir.launcher.ui.home

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.kvasir.launcher.data.apps.AppDetailsNavigator
import app.kvasir.launcher.data.apps.LauncherAppsRepository
import app.kvasir.launcher.data.calendar.CalendarEventNavigator
import app.kvasir.launcher.data.calendar.CalendarEventsRepository
import app.kvasir.launcher.data.home.DefaultHomeRepository
import app.kvasir.launcher.data.prefs.PreferencesRepository
import app.kvasir.launcher.data.system.SystemPanels
import app.kvasir.launcher.domain.AppLabelFilter
import app.kvasir.launcher.domain.FavoritesResolver
import app.kvasir.launcher.domain.model.Habit
import app.kvasir.launcher.domain.model.InstalledApp
import app.kvasir.launcher.domain.model.NextCalendarEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Spec 003 favorites + Spec 005 habits + Spec 008 / RF-008-01, RF-008-06 +
 * Spec 012 / RF-012-02, RF-012-04, RF-012-06 —
 * Home UI state; Composables never call DataStore / LauncherApps / CalendarContract.
 */
data class HomeHabitRow(
    val habit: Habit,
    val completed: Boolean,
)

data class HomeUiState(
    val showDefaultHomeCta: Boolean = false,
    val searchQuery: String = "",
    val favorites: List<InstalledApp> = emptyList(),
    /** Favorites before search filter (for empty vs no-match copy). */
    val hasAnyFavorites: Boolean = false,
    val favoritesReady: Boolean = false,
    val habitRows: List<HomeHabitRow> = emptyList(),
)

class HomeViewModel(
    application: Application,
    private val defaultHomeRepository: DefaultHomeRepository,
    private val launcherAppsRepository: LauncherAppsRepository,
    private val preferencesRepository: PreferencesRepository,
    private val calendarEventsRepository: CalendarEventsRepository,
) : AndroidViewModel(application) {

    private val defaultHomeCta = MutableStateFlow(false)
    /** Spec 008 / RF-008-05 — ephemeral; not persisted. */
    private val searchQuery = MutableStateFlow("")

    /** Spec 012 — separate from uiState so clock tick / favorites do not share this refresh. */
    private val nextEventInternal = MutableStateFlow<NextCalendarEvent?>(null)
    val nextEvent: StateFlow<NextCalendarEvent?> = nextEventInternal.asStateFlow()

    private data class HomeBase(
        val showCta: Boolean,
        val resolvedFavorites: List<InstalledApp>,
        val favoritesReady: Boolean,
        val habitRows: List<HomeHabitRow>,
    )

    val uiState: StateFlow<HomeUiState> = combine(
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
            HomeBase(
                showCta = showCta,
                resolvedFavorites = resolved,
                favoritesReady = snapshot.appsLoaded,
                habitRows = habits.map { habit ->
                    HomeHabitRow(
                        habit = habit,
                        completed = habit.id in dayState.completedIds,
                    )
                },
            )
        },
        searchQuery,
    ) { base, query ->
        HomeUiState(
            showDefaultHomeCta = base.showCta,
            searchQuery = query,
            favorites = AppLabelFilter.filterByQuery(base.resolvedFavorites, query),
            hasAnyFavorites = base.resolvedFavorites.isNotEmpty(),
            favoritesReady = base.favoritesReady,
            habitRows = base.habitRows,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    /** RF-001-09 — re-evaluate default-Home role when Activity resumes. */
    fun onResume() {
        val isDefault = defaultHomeRepository.isDefaultHome()
        defaultHomeCta.update { !isDefault }
        refreshNextEvent()
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

    /** Spec 008 / RF-008-01 — ephemeral search query. */
    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    /** Spec 009 / RF-009-01 — QS then notifications; no-op safe (outside Compose). */
    fun expandSystemPanel() {
        SystemPanels.expandPreferredPanel(getApplication())
    }

    /** Spec 009 / RF-009-04, RF-009-05 — system app details; does not mutate favorites. */
    fun openAppDetails(app: InstalledApp) {
        AppDetailsNavigator.open(getApplication(), app.packageName)
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

    companion object {
        fun factory(
            defaultHomeRepository: DefaultHomeRepository,
            launcherAppsRepository: LauncherAppsRepository,
            preferencesRepository: PreferencesRepository,
            calendarEventsRepository: CalendarEventsRepository,
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
                    )
                }
            }
    }
}
