package app.kvasir.launcher

import android.app.Application
import app.kvasir.launcher.di.AppContainer

/**
 * Spec 001 / RF-001-10 + Spec 002 / RF-002-11 —
 * Application hosts the manual DI container and starts LauncherApps observation once.
 */
class KvasirApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.launcherAppsRepository.start()
    }
}
