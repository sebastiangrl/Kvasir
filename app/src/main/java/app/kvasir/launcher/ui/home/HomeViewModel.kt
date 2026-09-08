package app.kvasir.launcher.ui.home

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
import app.kvasir.launcher.domain.FavoritesResolver
import app.kvasir.launcher.domain.model.Habit
import app.kvasir.launcher.domain.model.InstalledApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Spec 003 favorites + Spec 005 / RF-005-02, RF-005-03, RF-005-06, RF-005-07 —
 * Home UI state; Composables never call DataStore / LauncherApps.
 */
data class HomeHabitRow(
    val habit: Habit,
    val completed: Boolean,
)

data class HomeUiState(
    val showDefaultHomeCta: Boolean = false,
    val favorites: List<InstalledApp> = emptyList(),
    val favoritesReady: Boolean = false,
    val habitRows: List<HomeHabitRow> = emptyList(),
)

class HomeViewModel(
    application: Application,
    private val defaultHomeRepository: DefaultHomeRepository,
    private val launcherAppsRepository: LauncherAppsRepository,
    private val preferencesRepository: PreferencesRepository,
) : AndroidViewModel(application) {

    private val defaultHomeCta = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        defaultHomeCta,
        launcherAppsRepository.snapshot,
        preferencesRepository.favoriteKeys,
        preferencesRepository.habits,
        preferencesRepository.habitDayState,
    ) { showCta, snapshot, favoriteKeys, habits, dayState ->
        HomeUiState(
            showDefaultHomeCta = showCta,
            favorites = FavoritesResolver.resolve(
                favoriteKeys = favoriteKeys,
                installed = snapshot.apps,
            ),
            favoritesReady = snapshot.appsLoaded,
            habitRows = habits.map { habit ->
                HomeHabitRow(
                    habit = habit,
                    completed = habit.id in dayState.completedIds,
                )
            },
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
                    )
                }
            }
    }
}
