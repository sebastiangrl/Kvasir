package app.kvasir.launcher.data.apps

import android.content.pm.LauncherActivityInfo
import app.kvasir.launcher.domain.model.InstalledApp
import java.text.Collator
import java.util.Locale

/**
 * Spec 002 / RF-002-01, RF-002-04, RF-002-05 —
 * Pure filter/sort over [InstalledApp]; thin map from [LauncherActivityInfo].
 */
object InstalledAppMapper {

    fun fromLauncherActivityInfo(info: LauncherActivityInfo): InstalledApp {
        val componentName = info.componentName
        return InstalledApp(
            packageName = componentName.packageName,
            activityClassName = componentName.className,
            label = info.label?.toString().orEmpty(),
        )
    }

    /**
     * Excludes [selfPackageName] and sorts by [InstalledApp.label]
     * case-insensitive for [locale] (Collator PRIMARY).
     */
    fun prepareInstalledApps(
        apps: List<InstalledApp>,
        selfPackageName: String,
        locale: Locale = Locale.getDefault(),
    ): List<InstalledApp> {
        val collator = Collator.getInstance(locale).apply {
            strength = Collator.PRIMARY
        }
        return apps
            .asSequence()
            .filter { it.packageName != selfPackageName }
            .sortedWith { a, b -> collator.compare(a.label, b.label) }
            .toList()
    }
}
