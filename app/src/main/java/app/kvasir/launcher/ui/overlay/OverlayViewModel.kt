package app.kvasir.launcher.ui.overlay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.kvasir.launcher.data.apps.LauncherAppsRepository
import app.kvasir.launcher.domain.LetterBucket
import app.kvasir.launcher.domain.model.InstalledApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Spec 004 / RF-004-03, RF-004-06, RF-004-07, RF-004-08 —
 * Overlay open state + letter filter; no PreferencesRepository (does not mutate favorites).
 */
data class OverlayUiState(
    val isOpen: Boolean = false,
    val selectedLetter: Char = 'A',
    val filteredApps: List<InstalledApp> = emptyList(),
    val appsLoaded: Boolean = false,
)

class OverlayViewModel(
    private val launcherAppsRepository: LauncherAppsRepository,
) : ViewModel() {

    private val isOpen = MutableStateFlow(false)
    private val selectedLetter = MutableStateFlow('A')

    val uiState: StateFlow<OverlayUiState> = combine(
        isOpen,
        selectedLetter,
        launcherAppsRepository.snapshot,
    ) { open, letter, snapshot ->
        OverlayUiState(
            isOpen = open,
            selectedLetter = letter,
            filteredApps = if (open) {
                LetterBucket.filterByLetter(snapshot.apps, letter)
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
        selectedLetter.value = 'A'
        isOpen.value = true
    }

    fun close() {
        isOpen.value = false
        selectedLetter.value = 'A'
    }

    fun selectLetter(letter: Char) {
        selectedLetter.update { letter }
    }

    /** RF-004-03 + RF-004-02 (close after launch) — launch via repo, then close. */
    fun launchAndClose(app: InstalledApp) {
        launcherAppsRepository.launch(app)
        close()
    }

    companion object {
        fun factory(
            launcherAppsRepository: LauncherAppsRepository,
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    OverlayViewModel(
                        launcherAppsRepository = launcherAppsRepository,
                    )
                }
            }
    }
}
