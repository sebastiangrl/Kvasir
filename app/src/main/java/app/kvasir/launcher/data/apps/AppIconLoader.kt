package app.kvasir.launcher.data.apps

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.os.Process
import android.util.Log
import android.util.LruCache
import app.kvasir.launcher.domain.model.InstalledApp

/**
 * Spec 010 / RF-010-03, RF-010-05, RF-010-06 —
 * Loads activity icons via LauncherApps (Application context). Bounded LRU of Drawables.
 */
class AppIconLoader(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val launcherApps =
        appContext.getSystemService(LauncherApps::class.java)
            ?: error("LauncherApps service unavailable")
    private val userHandle = Process.myUserHandle()
    private val density = appContext.resources.displayMetrics.densityDpi

    /** Spec 010 / RF-010-06 — capped; not an unlimited Bitmap map. */
    private val cache = LruCache<String, Drawable>(MAX_CACHE_ENTRIES)

    fun loadDrawable(app: InstalledApp): Drawable? {
        cache.get(app.componentKey)?.let { return it }
        return try {
            val component = ComponentName(app.packageName, app.activityClassName)
            val info = launcherApps.getActivityList(app.packageName, userHandle)
                .firstOrNull { it.componentName == component }
                ?: return null
            val drawable = info.getBadgedIcon(density) ?: return null
            cache.put(app.componentKey, drawable)
            drawable
        } catch (error: Exception) {
            Log.w(TAG, "Failed to load icon for ${app.componentKey}", error)
            null
        }
    }

    companion object {
        private const val TAG = "AppIconLoader"
        private const val MAX_CACHE_ENTRIES = 48
    }
}
