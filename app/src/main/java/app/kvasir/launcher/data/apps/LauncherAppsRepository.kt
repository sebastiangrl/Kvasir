package app.kvasir.launcher.data.apps

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import app.kvasir.launcher.domain.model.InstalledApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Spec 002 / RF-002-02, RF-002-06, RF-002-07, RF-002-11 —
 * In-memory launchable apps via LauncherApps; Application context only.
 */
data class InstalledAppsSnapshot(
    val apps: List<InstalledApp> = emptyList(),
    val appsLoaded: Boolean = false,
)

class LauncherAppsRepository(
    context: Context,
    private val scope: CoroutineScope,
) {
    private val appContext = context.applicationContext
    private val launcherApps =
        appContext.getSystemService(LauncherApps::class.java)
            ?: error("LauncherApps service unavailable")
    private val userHandle = Process.myUserHandle()
    private val selfPackageName = appContext.packageName

    private val _snapshot = MutableStateFlow(InstalledAppsSnapshot())
    val snapshot: StateFlow<InstalledAppsSnapshot> = _snapshot.asStateFlow()

    private val started = AtomicBoolean(false)

    private val callback = object : LauncherApps.Callback() {
        override fun onPackageRemoved(packageName: String?, user: android.os.UserHandle?) {
            refresh()
        }

        override fun onPackageAdded(packageName: String?, user: android.os.UserHandle?) {
            refresh()
        }

        override fun onPackageChanged(packageName: String?, user: android.os.UserHandle?) {
            refresh()
        }

        override fun onPackagesAvailable(
            packageNames: Array<out String>?,
            user: android.os.UserHandle?,
            replacing: Boolean,
        ) {
            refresh()
        }

        override fun onPackagesUnavailable(
            packageNames: Array<out String>?,
            user: android.os.UserHandle?,
            replacing: Boolean,
        ) {
            refresh()
        }
    }

    /** RF-002-11 — register callback once for the process lifetime. */
    fun start() {
        if (!started.compareAndSet(false, true)) return
        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        refresh()
    }

    fun launch(app: InstalledApp) {
        try {
            launcherApps.startMainActivity(
                ComponentName(app.packageName, app.activityClassName),
                userHandle,
                null,
                null,
            )
        } catch (error: Exception) {
            // RF-002-07 — never crash the Home process on launch failure.
            Log.w(TAG, "Failed to launch ${app.componentKey}", error)
        }
    }

    private fun refresh() {
        scope.launch(Dispatchers.Default) {
            val apps = try {
                val raw = launcherApps.getActivityList(null, userHandle)
                    .map(InstalledAppMapper::fromLauncherActivityInfo)
                InstalledAppMapper.prepareInstalledApps(
                    apps = raw,
                    selfPackageName = selfPackageName,
                )
            } catch (error: Exception) {
                Log.e(TAG, "Failed to load launchable activities", error)
                emptyList()
            }
            _snapshot.value = InstalledAppsSnapshot(apps = apps, appsLoaded = true)
        }
    }

    private companion object {
        const val TAG = "LauncherAppsRepo"
    }
}
