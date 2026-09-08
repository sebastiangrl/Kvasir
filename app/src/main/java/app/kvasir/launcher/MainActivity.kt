package app.kvasir.launcher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.kvasir.launcher.domain.model.ThemeMode
import app.kvasir.launcher.ui.KvasirRoot
import app.kvasir.launcher.ui.home.HomeViewModel
import app.kvasir.launcher.ui.icons.LocalAppIconLoader
import app.kvasir.launcher.ui.settings.SettingsRuntimePermission
import app.kvasir.launcher.ui.settings.SettingsViewModel
import app.kvasir.launcher.ui.theme.KvasirTheme
import app.kvasir.launcher.ui.theme.kvasirScreenGradient

/**
 * Spec 001–006 + Spec 010 + Spec 012 + Spec 013 + Spec 015 + Spec 016 / RF-016-04 +
 * Spec 019 / RF-019-01, RF-019-05 —
 * HOME Activity hosts [KvasirRoot]; theme via Compose only.
 * Composables do not call DataStore / LauncherApps / CalendarContract / AlarmManager.
 */
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels {
        val app = application as KvasirApp
        HomeViewModel.factory(
            defaultHomeRepository = app.container.defaultHomeRepository,
            launcherAppsRepository = app.container.launcherAppsRepository,
            preferencesRepository = app.container.preferencesRepository,
            calendarEventsRepository = app.container.calendarEventsRepository,
            pomodoroController = app.container.pomodoroController,
            notificationBadgeRepository = app.container.notificationBadgeRepository,
            appShortcutsRepository = app.container.appShortcutsRepository,
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        val app = application as KvasirApp
        SettingsViewModel.factory(
            launcherAppsRepository = app.container.launcherAppsRepository,
            preferencesRepository = app.container.preferencesRepository,
            defaultHomeRepository = app.container.defaultHomeRepository,
            permissionsStatusHelper = app.container.permissionsStatusHelper,
        )
    }

    /** Spec 012 — ask READ_CALENDAR at most once per process (no Home CTA). */
    private var calendarPermissionRequestedThisProcess = false

    private val requestCalendarPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        homeViewModel.refreshNextEvent()
        settingsViewModel.refreshPermissions()
    }

    /** Spec 015 / RF-015-06 — on first Iniciar; settle start whether granted or denied. */
    private val requestNotificationsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        homeViewModel.onNotificationPermissionSettled()
        settingsViewModel.refreshPermissions()
    }

    /** Spec 016 — hub CTAs (do not start Pomodoro). */
    private val hubCalendarPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        homeViewModel.refreshNextEvent()
        settingsViewModel.refreshPermissions()
    }

    private val hubNotificationsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        settingsViewModel.refreshPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Spec 019 / RF-019-01 — draw under status/nav; bars stay transparent.
        enableEdgeToEdge()

        // RF-001-05 — on Home, consume Back; stay on Home (do not finish).
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(enabled = true) {
                override fun handleOnBackPressed() {
                    // Intentionally empty: do not finish() the HOME Activity.
                }
            },
        )

        setContent {
            val app = application as KvasirApp
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()

            LaunchedEffect(homeViewModel) {
                homeViewModel.notificationPermissionRequests.collect {
                    maybeRequestNotificationsThenStart()
                }
            }

            LaunchedEffect(settingsViewModel) {
                settingsViewModel.runtimePermissionRequests.collect { request ->
                    when (request) {
                        SettingsRuntimePermission.Calendar ->
                            hubCalendarPermission.launch(Manifest.permission.READ_CALENDAR)
                        SettingsRuntimePermission.Notifications -> {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                                settingsViewModel.refreshPermissions()
                            } else {
                                hubNotificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }
                }
            }

            val darkTheme = themeMode == ThemeMode.Dark
            DisposableEffect(darkTheme) {
                applyTransparentSystemBars(darkTheme)
                onDispose { }
            }

            CompositionLocalProvider(
                LocalAppIconLoader provides app.container.appIconLoader,
            ) {
                KvasirTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(kvasirScreenGradient(darkTheme)),
                        color = Color.Transparent,
                    ) {
                        KvasirRoot(
                            homeViewModel = homeViewModel,
                            settingsViewModel = settingsViewModel,
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.onResume()
        settingsViewModel.refreshPermissions()
        maybeRequestCalendarPermissionOnce()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Spec 013 / RF-013-07 — warm Home returns to ★.
        homeViewModel.resetToFavorites()
    }

    /** Spec 012 / RF-012-02 — system dialog once per process if not already granted. */
    private fun maybeRequestCalendarPermissionOnce() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CALENDAR,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return
        if (calendarPermissionRequestedThisProcess) return
        calendarPermissionRequestedThisProcess = true
        requestCalendarPermission.launch(Manifest.permission.READ_CALENDAR)
    }

    /** Spec 019 / RF-019-01 — icons follow ThemeMode, not system night (006 is Compose-only). */
    private fun applyTransparentSystemBars(darkTheme: Boolean) {
        val transparent = android.graphics.Color.TRANSPARENT
        enableEdgeToEdge(
            statusBarStyle = if (darkTheme) {
                SystemBarStyle.dark(transparent)
            } else {
                SystemBarStyle.light(transparent, transparent)
            },
            navigationBarStyle = if (darkTheme) {
                SystemBarStyle.dark(transparent)
            } else {
                SystemBarStyle.light(transparent, transparent)
            },
        )
    }

    /** Spec 015 / RF-015-06 — request only when needed; always start afterward. */
    private fun maybeRequestNotificationsThenStart() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            homeViewModel.onNotificationPermissionSettled()
            return
        }
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            homeViewModel.onNotificationPermissionSettled()
        } else {
            requestNotificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
