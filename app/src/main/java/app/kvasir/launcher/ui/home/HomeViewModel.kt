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
import app.kvasir.launcher.domain.model.InstalledApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Spec 001 CTA + Spec 002 / RF-002-08, RF-002-09, RF-002-10 —
 * Home UI state; Composables never call LauncherApps / PackageManager.
 */
data class HomeUiState(
    val showDefaultHomeCta: Boolean = false,
    val installedApps: List<InstalledApp> = emptyList(),
    val appsLoaded: Boolean = false,
)

class HomeViewModel(
    application: Application,
    private val defaultHomeRepository: DefaultHomeRepository,
    private val launcherAppsRepository: LauncherAppsRepository,
) : AndroidViewModel(application) {

    private val defaultHomeCta = MutableStateFlow(false)

    val uiState: StateFlow<HomeUiState> = combine(
        defaultHomeCta,
        launcherAppsRepository.snapshot,
    ) { showCta, snapshot ->
        HomeUiState(
            showDefaultHomeCta = showCta,
            installedApps = snapshot.apps,
            appsLoaded = snapshot.appsLoaded,
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

    /** RF-002-07 — launch via repository (safe for Home process). */
    fun launchApp(app: InstalledApp) {
        launcherAppsRepository.launch(app)
    }

    companion object {
        fun factory(
            defaultHomeRepository: DefaultHomeRepository,
            launcherAppsRepository: LauncherAppsRepository,
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as Application
                    HomeViewModel(
                        application = application,
                        defaultHomeRepository = defaultHomeRepository,
                        launcherAppsRepository = launcherAppsRepository,
                    )
                }
            }
    }
}
