package app.kvasir.launcher.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.kvasir.launcher.data.apps.LauncherAppsRepository
import app.kvasir.launcher.data.prefs.PreferencesRepository
import app.kvasir.launcher.domain.model.Habit
import app.kvasir.launcher.domain.model.InstalledApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Spec 003 favorites + Spec 005 / RF-005-05 —
 * Settings catalog + habit CRUD; Composables never touch DataStore / LauncherApps.
 */
data class SettingsFavoriteRow(
    val app: InstalledApp,
    val isFavorite: Boolean,
)

data class SettingsUiState(
    val rows: List<SettingsFavoriteRow> = emptyList(),
    val appsLoaded: Boolean = false,
    val habits: List<Habit> = emptyList(),
)

class SettingsViewModel(
    private val launcherAppsRepository: LauncherAppsRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        launcherAppsRepository.snapshot,
        preferencesRepository.favoriteKeys,
        preferencesRepository.habits,
    ) { snapshot, favoriteKeys, habits ->
        SettingsUiState(
            rows = snapshot.apps.map { app ->
                SettingsFavoriteRow(
                    app = app,
                    isFavorite = app.componentKey in favoriteKeys,
                )
            },
            appsLoaded = snapshot.appsLoaded,
            habits = habits,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    /** Prune orphan favorite keys when entering Settings. */
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
