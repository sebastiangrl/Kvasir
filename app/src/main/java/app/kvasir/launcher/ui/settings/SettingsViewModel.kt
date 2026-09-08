package app.kvasir.launcher.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.kvasir.launcher.data.apps.LauncherAppsRepository
import app.kvasir.launcher.data.prefs.PreferencesRepository
import app.kvasir.launcher.domain.HabitStreak
import app.kvasir.launcher.domain.model.Habit
import app.kvasir.launcher.domain.model.InstalledApp
import app.kvasir.launcher.domain.model.PomodoroConfig
import app.kvasir.launcher.domain.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Spec 003 + Spec 005 + Spec 006 + Spec 014 / RF-014-05 + Spec 015 / RF-015-04 —
 * Settings catalog, habits, theme, Pomodoro config; Composables never touch DataStore / LauncherApps.
 */
data class SettingsFavoriteRow(
    val app: InstalledApp,
    val isFavorite: Boolean,
)

data class SettingsHabitRow(
    val habit: Habit,
    val streak: Int,
    /** Index 0 = six days ago … 6 = today. */
    val lastSevenDays: List<Boolean>,
)

data class SettingsUiState(
    val rows: List<SettingsFavoriteRow> = emptyList(),
    val appsLoaded: Boolean = false,
    val habitRows: List<SettingsHabitRow> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.Light,
    val pomodoroConfig: PomodoroConfig = PomodoroConfig.Default,
)

class SettingsViewModel(
    private val launcherAppsRepository: LauncherAppsRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = preferencesRepository.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeMode.Light,
    )

    private data class SettingsCatalog(
        val rows: List<SettingsFavoriteRow>,
        val appsLoaded: Boolean,
        val habitRows: List<SettingsHabitRow>,
    )

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            launcherAppsRepository.snapshot,
            preferencesRepository.favoriteKeys,
            preferencesRepository.habits,
            preferencesRepository.habitDayState,
            preferencesRepository.habitHistory,
        ) { snapshot, favoriteKeys, habits, dayState, history ->
            val today = LocalDate.now().toEpochDay()
            SettingsCatalog(
                rows = snapshot.apps.map { app ->
                    SettingsFavoriteRow(
                        app = app,
                        isFavorite = app.componentKey in favoriteKeys,
                    )
                },
                appsLoaded = snapshot.appsLoaded,
                habitRows = habits.map { habit ->
                    val completed = habit.id in dayState.completedIds
                    val days = history[habit.id].orEmpty()
                    SettingsHabitRow(
                        habit = habit,
                        streak = HabitStreak.streak(
                            historyDays = days,
                            todayCompleted = completed,
                            todayEpochDay = today,
                        ),
                        lastSevenDays = HabitStreak.lastSevenDays(
                            historyDays = days,
                            todayCompleted = completed,
                            todayEpochDay = today,
                        ),
                    )
                },
            )
        },
        preferencesRepository.themeMode,
        preferencesRepository.pomodoroConfig,
    ) { catalog, theme, pomodoro ->
        SettingsUiState(
            rows = catalog.rows,
            appsLoaded = catalog.appsLoaded,
            habitRows = catalog.habitRows,
            themeMode = theme,
            pomodoroConfig = pomodoro,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun onSettingsOpened() {
        viewModelScope.launch {
            val snapshot = launcherAppsRepository.snapshot.value
            val apps = if (snapshot.appsLoaded) {
                snapshot.apps
            } else {
                launcherAppsRepository.snapshot.first { it.appsLoaded }.apps
            }
            val installedKeys = apps.map { it.componentKey }.toSet()
            val current = preferencesRepository.favoriteKeys.first()
            val pruned = current.intersect(installedKeys)
            if (pruned != current) {
                preferencesRepository.setFavoriteKeys(pruned)
            }
        }
    }

    fun setFavorite(componentKey: String, favorite: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setFavorite(componentKey, favorite)
        }
    }

    fun addHabit(label: String) {
        viewModelScope.launch {
            if (label.isBlank()) return@launch
            preferencesRepository.addHabit(label)
        }
    }

    fun removeHabit(habitId: String) {
        viewModelScope.launch {
            preferencesRepository.removeHabit(habitId)
        }
    }

    fun renameHabit(habitId: String, newLabel: String) {
        viewModelScope.launch {
            preferencesRepository.renameHabit(habitId, newLabel)
        }
    }

    fun setDarkTheme(dark: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(
                if (dark) ThemeMode.Dark else ThemeMode.Light,
            )
        }
    }

    /** Spec 015 / RF-015-04 — clamp and persist Pomodoro config fields. */
    fun setPomodoroWorkMinutes(minutes: Int) {
        viewModelScope.launch {
            val current = preferencesRepository.getPomodoroConfig()
            preferencesRepository.setPomodoroConfig(current.copy(workMinutes = minutes))
        }
    }

    fun setPomodoroBreakMinutes(minutes: Int) {
        viewModelScope.launch {
            val current = preferencesRepository.getPomodoroConfig()
            preferencesRepository.setPomodoroConfig(current.copy(breakMinutes = minutes))
        }
    }

    fun setPomodoroSessions(sessions: Int) {
        viewModelScope.launch {
            val current = preferencesRepository.getPomodoroConfig()
            preferencesRepository.setPomodoroConfig(current.copy(sessionsPerCycle = sessions))
        }
    }

    companion object {
        fun factory(
            launcherAppsRepository: LauncherAppsRepository,
            preferencesRepository: PreferencesRepository,
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    SettingsViewModel(
                        launcherAppsRepository = launcherAppsRepository,
                        preferencesRepository = preferencesRepository,
                    )
                }
            }
    }
}
