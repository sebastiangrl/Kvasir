package app.kvasir.launcher.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.kvasir.launcher.data.apps.LauncherAppsRepository
import app.kvasir.launcher.data.prefs.PreferencesRepository
import app.kvasir.launcher.domain.model.InstalledApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Spec 003 / RF-003-05, RF-003-08 —
 * Catalog rows for favorite toggles; Composables never touch DataStore / LauncherApps.
 */
data class SettingsFavoriteRow(
    val app: InstalledApp,
    val isFavorite: Boolean,
)

data class SettingsUiState(
    val rows: List<SettingsFavoriteRow> = emptyList(),
    val appsLoaded: Boolean = false,
)

class SettingsViewModel(
    private val launcherAppsRepository: LauncherAppsRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        launcherAppsRepository.snapshot,
        preferencesRepository.favoriteKeys,
    ) { snapshot, favoriteKeys ->
        SettingsUiState(
            rows = snapshot.apps.map { app ->
                SettingsFavoriteRow(
                    app = app,
                    isFavorite = app.componentKey in favoriteKeys,
                )
            },
            appsLoaded = snapshot.appsLoaded,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    /** Prune orphan favorite keys when entering Settings (plan decisión 4). */
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
