package app.kvasir.launcher.ui.settings

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.kvasir.launcher.data.apps.LauncherAppsRepository
import app.kvasir.launcher.data.home.DefaultHomeRepository
import app.kvasir.launcher.data.prefs.PreferencesRepository
import app.kvasir.launcher.data.settings.AppSettingsNavigator
import app.kvasir.launcher.data.settings.PermissionsStatus
import app.kvasir.launcher.data.settings.PermissionsStatusHelper
import app.kvasir.launcher.domain.HabitStreak
import app.kvasir.launcher.domain.model.Habit
import app.kvasir.launcher.domain.model.InstalledApp
import app.kvasir.launcher.domain.model.PomodoroConfig
import app.kvasir.launcher.domain.model.SettingsSection
import app.kvasir.launcher.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Spec 003 + Spec 005 + Spec 006 + Spec 014 + Spec 015 + Spec 016 / RF-016-01…05 —
 * Settings hub + permissions CTAs; Composables never touch DataStore / PM / Settings.
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

/** Spec 016 — hub asks Activity to run a runtime permission dialog. */
enum class SettingsRuntimePermission {
    Calendar,
    Notifications,
}

class SettingsViewModel(
    application: Application,
    private val launcherAppsRepository: LauncherAppsRepository,
    private val preferencesRepository: PreferencesRepository,
    private val defaultHomeRepository: DefaultHomeRepository,
    private val permissionsStatusHelper: PermissionsStatusHelper,
) : AndroidViewModel(application) {

    val themeMode: StateFlow<ThemeMode> = preferencesRepository.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeMode.Light,
    )

    /** Spec 016 — null = hub index; otherwise open section. */
    private val openSectionInternal = MutableStateFlow<SettingsSection?>(null)
    val openSection: StateFlow<SettingsSection?> = openSectionInternal.asStateFlow()

    private val permissionsStatusInternal = MutableStateFlow(
        PermissionsStatus(
            isDefaultHome = false,
            calendarGranted = false,
            notificationsGranted = false,
            exactAlarmApplicable = false,
            exactAlarmGranted = true,
        ),
    )
    val permissionsStatus: StateFlow<PermissionsStatus> = permissionsStatusInternal.asStateFlow()

    private val runtimePermissionRequestsInternal =
        MutableSharedFlow<SettingsRuntimePermission>(extraBufferCapacity = 1)
    val runtimePermissionRequests = runtimePermissionRequestsInternal.asSharedFlow()

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
        openSectionInternal.value = null
        refreshPermissions()
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

    fun openSection(section: SettingsSection) {
        openSectionInternal.value = section
        if (section == SettingsSection.Permissions) {
            refreshPermissions()
        }
    }

    /** Spec 016 / RF-016-04 — re-read after resume or permission dialog. */
    fun refreshPermissions() {
        permissionsStatusInternal.value = permissionsStatusHelper.snapshot()
    }

    /** @return true if handled (returned to hub); false if already on hub (caller leaves Settings). */
    fun navigateBackWithinSettings(): Boolean {
        if (openSectionInternal.value != null) {
            openSectionInternal.value = null
            return true
        }
        return false
    }

    fun openHomePicker() {
        val intent = defaultHomeRepository.createHomePickerIntent()
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        getApplication<Application>().startActivity(intent)
    }

    fun requestCalendarPermission() {
        runtimePermissionRequestsInternal.tryEmit(SettingsRuntimePermission.Calendar)
    }

    fun requestNotificationsPermission() {
        runtimePermissionRequestsInternal.tryEmit(SettingsRuntimePermission.Notifications)
    }

    fun openAppDetailsSettings() {
        AppSettingsNavigator.openAppDetails(
            getApplication(),
            getApplication<Application>().packageName,
        )
    }

    fun openNotificationSettings() {
        AppSettingsNavigator.openNotificationSettings(
            getApplication(),
            getApplication<Application>().packageName,
        )
    }

    fun openExactAlarmSettings() {
        AppSettingsNavigator.openExactAlarmSettings(
            getApplication(),
            getApplication<Application>().packageName,
        )
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
            defaultHomeRepository: DefaultHomeRepository,
            permissionsStatusHelper: PermissionsStatusHelper,
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as Application
                    SettingsViewModel(
                        application = application,
                        launcherAppsRepository = launcherAppsRepository,
                        preferencesRepository = preferencesRepository,
                        defaultHomeRepository = defaultHomeRepository,
                        permissionsStatusHelper = permissionsStatusHelper,
                    )
                }
            }
    }
}
