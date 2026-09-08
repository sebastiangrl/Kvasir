package app.kvasir.launcher.ui.overlay

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.kvasir.launcher.data.apps.AppDetailsNavigator
import app.kvasir.launcher.data.apps.LauncherAppsRepository
import app.kvasir.launcher.domain.AppLabelFilter
import app.kvasir.launcher.domain.model.InstalledApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Spec 004 scrubber + Spec 008 search + Spec 009 / RF-009-04, RF-009-05 —
 * Overlay open + letter/query filter; app details; no PreferencesRepository.
 */
data class OverlayUiState(
    val isOpen: Boolean = false,
    val selectedLetter: Char = 'A',
    val searchQuery: String = "",
    val filteredApps: List<InstalledApp> = emptyList(),
    val appsLoaded: Boolean = false,
)

class OverlayViewModel(
    private val appContext: Context,
    private val launcherAppsRepository: LauncherAppsRepository,
) : ViewModel() {

    private val isOpen = MutableStateFlow(false)
    private val selectedLetter = MutableStateFlow('A')
    /** Spec 008 / RF-008-05 — ephemeral; cleared on open/close. */
    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<OverlayUiState> = combine(
        isOpen,
        selectedLetter,
        searchQuery,
        launcherAppsRepository.snapshot,
    ) { open, letter, query, snapshot ->
        OverlayUiState(
            isOpen = open,
            selectedLetter = letter,
            searchQuery = query,
            filteredApps = if (open) {
                AppLabelFilter.filterOverlay(snapshot.apps, query, letter)
            } else {
                emptyList()
            },
            appsLoaded = snapshot.appsLoaded,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OverlayUiState(),
    )

    fun open() {
        searchQuery.value = ""
        selectedLetter.value = 'A'
        isOpen.value = true
    }

    fun close() {
        isOpen.value = false
        selectedLetter.value = 'A'
        searchQuery.value = ""
    }

    fun selectLetter(letter: Char) {
        // Returning to scrubber mode clears text search (RF-008-04).
        searchQuery.value = ""
        selectedLetter.update { letter }
    }

    /** Spec 008 / RF-008-03 — typing resets letter; query wins in [AppLabelFilter.filterOverlay]. */
    fun setSearchQuery(query: String) {
        searchQuery.value = query
        if (query.trim().isNotEmpty()) {
            selectedLetter.value = 'A'
        }
    }

    /** RF-004-03 + RF-004-02 (close after launch) — launch via repo, then close. */
    fun launchAndClose(app: InstalledApp) {
        launcherAppsRepository.launch(app)
        close()
    }

    /** Spec 009 / RF-009-04, RF-009-05 — details; does not close overlay or mutate favorites. */
    fun openAppDetails(app: InstalledApp) {
        AppDetailsNavigator.open(appContext, app.packageName)
    }

    companion object {
        fun factory(
            appContext: Context,
            launcherAppsRepository: LauncherAppsRepository,
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    OverlayViewModel(
                        appContext = appContext.applicationContext,
                        launcherAppsRepository = launcherAppsRepository,
                    )
                }
            }
    }
}
