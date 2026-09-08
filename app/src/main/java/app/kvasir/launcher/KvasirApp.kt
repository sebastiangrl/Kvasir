package app.kvasir.launcher

import android.app.Application
import app.kvasir.launcher.di.AppContainer

/**
 * Spec 001 / RF-001-10 — Application hosts the manual DI container.
 */
class KvasirApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
