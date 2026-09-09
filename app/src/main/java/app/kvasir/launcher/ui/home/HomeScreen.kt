package app.kvasir.launcher.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.kvasir.launcher.R
import app.kvasir.launcher.domain.NextEventLineFormat
import app.kvasir.launcher.domain.PomodoroPhaseMachine
import app.kvasir.launcher.domain.PomodoroRemainingFormat
import app.kvasir.launcher.domain.model.AppShortcut
import app.kvasir.launcher.domain.model.HomeListMode
import app.kvasir.launcher.domain.model.InstalledApp
import app.kvasir.launcher.domain.model.NextCalendarEvent
import app.kvasir.launcher.domain.model.PomodoroPhase
import app.kvasir.launcher.domain.model.PomodoroSession
import app.kvasir.launcher.domain.model.PomodoroStatus
import app.kvasir.launcher.ui.gesture.onVerticalSwipe
import app.kvasir.launcher.ui.icons.MonochromeAppIcon
import app.kvasir.launcher.ui.theme.KvasirPillShape
import app.kvasir.launcher.ui.theme.KvasirRoundCheckbox
import kotlinx.coroutines.delay

/**
 * Spec 003–012 + Spec 013 / RF-013-01…04 + Spec 015 / RF-015-03, RF-015-07 +
 * Spec 017 / RF-017-04…06 + Spec 018 / RF-018-02…05 +
 * Spec 019 / RF-019-01, RF-019-03, RF-019-04, RF-019-05, RF-019-06 +
 * Spec 020 / RF-020-01…03 —
 * Unified Home: ★ chrome or letter/search catalog + scrubber rail + Pomodoro + badges.
 * Search field is lifted above chrome/catalog so focus survives query → catalog switch
 * (RF-008 typing continuity).
 * No DataStore / PackageManager / LauncherApps / CalendarContract / AlarmManager /
 * NotificationListener here.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    showDefaultHomeCta: Boolean,
    onChooseHomeClick: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    favorites: List<InstalledApp>,
    hasAnyFavorites: Boolean,
    favoritesReady: Boolean,
    catalogApps: List<InstalledApp>,
    showingFavoritesChrome: Boolean,
    isQueryActive: Boolean,
    listMode: HomeListMode,
    onFavoriteClick: (InstalledApp) -> Unit,
    onFavoriteLongClick: (InstalledApp) -> Unit,
    onCatalogAppClick: (InstalledApp) -> Unit,
    onCatalogAppLongClick: (InstalledApp) -> Unit,
    habitRows: List<HomeHabitRow>,
    onHabitCheckedChange: (habitId: String, completed: Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    onExpandSystemPanel: () -> Unit,
    onSelectFavorites: () -> Unit,
    onSelectLetter: (Char) -> Unit,
    nextEvent: NextCalendarEvent? = null,
    onNextEventClick: () -> Unit = {},
    pomodoroSession: PomodoroSession = PomodoroSession.Idle,
    onPomodoroStart: () -> Unit = {},
    onPomodoroPause: () -> Unit = {},
    onPomodoroResume: () -> Unit = {},
    onPomodoroStop: () -> Unit = {},
    /** Spec 017 — packages with badge; only applied to ★ favorites rows. */
    badgedPackages: Set<String> = emptySet(),
    /** Spec 018 — non-null while shortcuts sheet is open. */
    shortcutsSheet: ShortcutsSheetState? = null,
    onDismissShortcutsSheet: () -> Unit = {},
    onShortcutClick: (AppShortcut) -> Unit = {},
    onShortcutsSheetDetailsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            // Spec 013 / RF-013-05 — swipe-up no longer opens apps overlay (no-op).
            .onVerticalSwipe(
                onSwipeDown = onExpandSystemPanel,
            ),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(top = 32.dp),
                verticalArrangement = Arrangement.Top,
            ) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 4.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings),
                        contentDescription = stringResource(R.string.settings),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Clock only in ★ chrome; search stays mounted across chrome ↔ catalog.
                if (showingFavoritesChrome) {
                    val eventLine = nextEvent?.let { NextEventLineFormat.format(it) }.orEmpty()
                    HomeClock(
                        eventLine = eventLine,
                        onEventClick = onNextEventClick,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    singleLine = true,
                    shape = KvasirPillShape,
                    placeholder = { Text(text = stringResource(R.string.search_apps_hint)) },
                )
                Spacer(
                    modifier = Modifier.height(if (showingFavoritesChrome) 24.dp else 12.dp),
                )

                if (showingFavoritesChrome) {
                    FavoritesHomeBody(
                        showDefaultHomeCta = showDefaultHomeCta,
                        onChooseHomeClick = onChooseHomeClick,
                        favorites = favorites,
                        hasAnyFavorites = hasAnyFavorites,
                        favoritesReady = favoritesReady,
                        onFavoriteClick = onFavoriteClick,
                        onFavoriteLongClick = onFavoriteLongClick,
                        habitRows = habitRows,
                        onHabitCheckedChange = onHabitCheckedChange,
                        pomodoroSession = pomodoroSession,
                        onPomodoroStart = onPomodoroStart,
                        onPomodoroPause = onPomodoroPause,
                        onPomodoroResume = onPomodoroResume,
                        onPomodoroStop = onPomodoroStop,
                        badgedPackages = badgedPackages,
                    )
                } else {
                    CatalogHomeBody(
                        listMode = listMode,
                        isQueryActive = isQueryActive,
                        catalogApps = catalogApps,
                        appsReady = favoritesReady,
                        onAppClick = onCatalogAppClick,
                        onAppLongClick = onCatalogAppLongClick,
                    )
                }
            }

            HomeScrubberRail(
                listMode = listMode,
                onSelectFavorites = onSelectFavorites,
                onSelectLetter = onSelectLetter,
                modifier = Modifier.fillMaxHeight(),
            )
        }

        // Spec 018 / RF-018-02, RF-018-03, RF-018-05 — text-only shortcuts sheet.
        if (shortcutsSheet != null) {
            ShortcutsSheetDialog(
                state = shortcutsSheet,
                onDismiss = onDismissShortcutsSheet,
                onShortcutClick = onShortcutClick,
                onDetailsClick = onShortcutsSheetDetailsClick,
            )
        }
    }
}

@Composable
private fun ShortcutsSheetDialog(
    state: ShortcutsSheetState,
    onDismiss: () -> Unit,
    onShortcutClick: (AppShortcut) -> Unit,
    onDetailsClick: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            ) {
                Text(
                    text = state.app.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
                state.shortcuts.forEach { shortcut ->
                    Text(
                        text = shortcut.label,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onShortcutClick(shortcut) }
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.shortcuts_app_details),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onDetailsClick)
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoritesHomeBody(
    showDefaultHomeCta: Boolean,
    onChooseHomeClick: () -> Unit,
    favorites: List<InstalledApp>,
    hasAnyFavorites: Boolean,
    favoritesReady: Boolean,
    onFavoriteClick: (InstalledApp) -> Unit,
    onFavoriteLongClick: (InstalledApp) -> Unit,
    habitRows: List<HomeHabitRow>,
    onHabitCheckedChange: (habitId: String, completed: Boolean) -> Unit,
    pomodoroSession: PomodoroSession,
    onPomodoroStart: () -> Unit,
    onPomodoroPause: () -> Unit,
    onPomodoroResume: () -> Unit,
    onPomodoroStop: () -> Unit,
    badgedPackages: Set<String>,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (showDefaultHomeCta) {
            Text(
                text = stringResource(R.string.home_not_default_message),
                modifier = Modifier.padding(horizontal = 24.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onChooseHomeClick,
                modifier = Modifier.padding(horizontal = 24.dp),
            ) {
                Text(text = stringResource(R.string.home_choose_default_action))
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (favoritesReady) {
                when {
                    !hasAnyFavorites -> {
                        item(key = "favorites_empty") {
                            Text(
                                text = stringResource(R.string.home_empty_favorites),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    favorites.isEmpty() -> {
                        item(key = "search_empty") {
                            Text(
                                text = stringResource(R.string.search_apps_empty),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    else -> {
                        items(
                            items = favorites,
                            key = { it.componentKey },
                        ) { app ->
                            AppRow(
                                app = app,
                                onClick = { onFavoriteClick(app) },
                                onLongClick = { onFavoriteLongClick(app) },
                                showBadge = app.packageName in badgedPackages,
                            )
                        }
                    }
                }
            }

            item(key = "pomodoro") {
                PomodoroHomeBlock(
                    session = pomodoroSession,
                    onStart = onPomodoroStart,
                    onPause = onPomodoroPause,
                    onResume = onPomodoroResume,
                    onStop = onPomodoroStop,
                )
            }

            item(key = "habits_header") {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.habits_section),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (habitRows.isEmpty()) {
                item(key = "habits_empty") {
                    Text(
                        text = stringResource(R.string.habits_empty),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(
                    items = habitRows,
                    key = { it.habit.id },
                ) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        KvasirRoundCheckbox(
                            checked = row.completed,
                            onCheckedChange = { checked ->
                                onHabitCheckedChange(row.habit.id, checked)
                            },
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (row.streak >= 1) {
                                "${row.habit.label} · ${row.streak}"
                            } else {
                                row.habit.label
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onHabitCheckedChange(row.habit.id, !row.completed)
                                }
                                .padding(end = 8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CatalogHomeBody(
    listMode: HomeListMode,
    isQueryActive: Boolean,
    catalogApps: List<InstalledApp>,
    appsReady: Boolean,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (!isQueryActive && listMode is HomeListMode.Letter) {
            Text(
                text = listMode.letter.toString(),
                modifier = Modifier.padding(horizontal = 24.dp),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        when {
            !appsReady -> Unit
            catalogApps.isEmpty() -> {
                Text(
                    text = stringResource(
                        if (isQueryActive) {
                            R.string.search_apps_empty
                        } else {
                            R.string.overlay_letter_empty
                        },
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    items(
                        items = catalogApps,
                        key = { it.componentKey },
                    ) { app ->
                        AppRow(
                            app = app,
                            onClick = { onAppClick(app) },
                            onLongClick = { onAppLongClick(app) },
                            showBadge = false,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppRow(
    app: InstalledApp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    showBadge: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MonochromeAppIcon(app = app)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = if (showBadge) {
                stringResource(R.string.home_app_badge_label, app.label)
            } else {
                app.label
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Spec 015 / RF-015-03, RF-015-07 —
 * Local 1 Hz tick only for this block; does not drive [HomeClock].
 */
@Composable
private fun PomodoroHomeBlock(
    session: PomodoroSession,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
) {
    var nowEpochMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(session.status, session.endsAtEpochMillis, session.remainingMillis) {
        if (session.status == PomodoroStatus.Running) {
            while (true) {
                nowEpochMillis = System.currentTimeMillis()
                delay(1_000L)
            }
        } else {
            nowEpochMillis = System.currentTimeMillis()
        }
    }
    val remaining = PomodoroPhaseMachine.remainingMillis(session, nowEpochMillis)
    val remainingLabel = PomodoroRemainingFormat.format(remaining)
    val statusText = when (session.status) {
        PomodoroStatus.Idle -> stringResource(R.string.pomodoro_status_idle)
        PomodoroStatus.Paused -> stringResource(R.string.pomodoro_status_paused, remainingLabel)
        PomodoroStatus.Running -> when (session.phase) {
            PomodoroPhase.Work -> stringResource(R.string.pomodoro_status_work, remainingLabel)
            PomodoroPhase.Break -> stringResource(R.string.pomodoro_status_break, remainingLabel)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (session.status) {
                PomodoroStatus.Idle -> {
                    TextButton(onClick = onStart) {
                        Text(text = stringResource(R.string.pomodoro_start))
                    }
                }
                PomodoroStatus.Running -> {
                    TextButton(onClick = onPause) {
                        Text(text = stringResource(R.string.pomodoro_pause))
                    }
                    TextButton(onClick = onStop) {
                        Text(text = stringResource(R.string.pomodoro_stop))
                    }
                }
                PomodoroStatus.Paused -> {
                    TextButton(onClick = onResume) {
                        Text(text = stringResource(R.string.pomodoro_resume))
                    }
                    TextButton(onClick = onStop) {
                        Text(text = stringResource(R.string.pomodoro_stop))
                    }
                }
            }
        }
    }
}
