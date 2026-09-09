package app.kvasir.launcher

import android.app.Application
import app.kvasir.launcher.di.AppContainer
import kotlinx.coroutines.launch

/**
 * Spec 001 / RF-001-10 + Spec 002 / RF-002-11 + Spec 015 / RF-015-02 +
 * Spec 021 / RF-021-08 —
 * Application hosts the manual DI container and starts LauncherApps observation once.
 */
class KvasirApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.launcherAppsRepository.start()
        container.pomodoroNotifier.ensureChannels()
        container.applicationScope.launch {
            container.pomodoroController.rescheduleIfNeeded()
        }
    }
}
