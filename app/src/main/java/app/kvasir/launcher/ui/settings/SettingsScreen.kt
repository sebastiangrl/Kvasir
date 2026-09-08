package app.kvasir.launcher.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.kvasir.launcher.R
import app.kvasir.launcher.data.settings.PermissionsStatus
import app.kvasir.launcher.domain.model.PomodoroConfig
import app.kvasir.launcher.domain.model.SettingsSection

/**
 * Spec 003 + Spec 005 + Spec 006 + Spec 014 + Spec 015 + Spec 016 / RF-016-01…05 —
 * Settings hub + sections; no DataStore / LauncherApps / PackageManager here.
 */
@Composable
fun SettingsScreen(
    section: SettingsSection?,
    onOpenSection: (SettingsSection) -> Unit,
    rows: List<SettingsFavoriteRow>,
    appsLoaded: Boolean,
    onFavoriteChange: (componentKey: String, favorite: Boolean) -> Unit,
    habitRows: List<SettingsHabitRow>,
    onAddHabit: (label: String) -> Unit,
    onRemoveHabit: (habitId: String) -> Unit,
    onRenameHabit: (habitId: String, label: String) -> Unit,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    pomodoroConfig: PomodoroConfig,
    onPomodoroWorkMinutes: (Int) -> Unit,
    onPomodoroBreakMinutes: (Int) -> Unit,
    onPomodoroSessions: (Int) -> Unit,
    permissionsStatus: PermissionsStatus,
    onOpenHomePicker: () -> Unit,
    onRequestCalendarPermission: () -> Unit,
    onRequestNotificationsPermission: () -> Unit,
    onOpenAppDetailsSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 8.dp),
    ) {
        val title = when (section) {
            null -> stringResource(R.string.settings)
            SettingsSection.Appearance -> stringResource(R.string.settings_section_appearance)
            SettingsSection.Pomodoro -> stringResource(R.string.pomodoro_section)
            SettingsSection.Favorites -> stringResource(R.string.settings_section_favorites)
            SettingsSection.Habits -> stringResource(R.string.habits_section)
            SettingsSection.Permissions -> stringResource(R.string.settings_section_permissions)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            TextButton(onClick = onBack) {
                Text(text = stringResource(R.string.settings_back))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        when (section) {
            null -> SettingsHub(onOpenSection = onOpenSection)
            SettingsSection.Appearance -> AppearanceSection(
                darkTheme = darkTheme,
                onDarkThemeChange = onDarkThemeChange,
            )
            SettingsSection.Pomodoro -> PomodoroSection(
                pomodoroConfig = pomodoroConfig,
                onPomodoroWorkMinutes = onPomodoroWorkMinutes,
                onPomodoroBreakMinutes = onPomodoroBreakMinutes,
                onPomodoroSessions = onPomodoroSessions,
            )
            SettingsSection.Favorites -> FavoritesSection(
                rows = rows,
                appsLoaded = appsLoaded,
                onFavoriteChange = onFavoriteChange,
            )
            SettingsSection.Habits -> HabitsSection(
                habitRows = habitRows,
                onAddHabit = onAddHabit,
                onRemoveHabit = onRemoveHabit,
                onRenameHabit = onRenameHabit,
            )
            SettingsSection.Permissions -> PermissionsSection(
                status = permissionsStatus,
                onOpenHomePicker = onOpenHomePicker,
                onRequestCalendarPermission = onRequestCalendarPermission,
                onRequestNotificationsPermission = onRequestNotificationsPermission,
                onOpenAppDetailsSettings = onOpenAppDetailsSettings,
                onOpenNotificationSettings = onOpenNotificationSettings,
                onOpenExactAlarmSettings = onOpenExactAlarmSettings,
            )
        }
    }
}

@Composable
private fun SettingsHub(
    onOpenSection: (SettingsSection) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item(key = "hub_appearance") {
            HubRow(
                title = stringResource(R.string.settings_section_appearance),
                subtitle = stringResource(R.string.settings_section_appearance_sub),
                onClick = { onOpenSection(SettingsSection.Appearance) },
            )
        }
        item(key = "hub_pomodoro") {
            HubRow(
                title = stringResource(R.string.pomodoro_section),
                subtitle = stringResource(R.string.settings_section_pomodoro_sub),
                onClick = { onOpenSection(SettingsSection.Pomodoro) },
            )
        }
        item(key = "hub_favorites") {
            HubRow(
                title = stringResource(R.string.settings_section_favorites),
                subtitle = stringResource(R.string.settings_section_favorites_sub),
                onClick = { onOpenSection(SettingsSection.Favorites) },
            )
        }
        item(key = "hub_habits") {
            HubRow(
                title = stringResource(R.string.habits_section),
                subtitle = stringResource(R.string.settings_section_habits_sub),
                onClick = { onOpenSection(SettingsSection.Habits) },
            )
        }
        item(key = "hub_permissions") {
            HubRow(
                title = stringResource(R.string.settings_section_permissions),
                subtitle = stringResource(R.string.settings_section_permissions_sub),
                onClick = { onOpenSection(SettingsSection.Permissions) },
            )
        }
        item(key = "hub_spacer") {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HubRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AppearanceSection(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.theme_dark),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Switch(
            checked = darkTheme,
            onCheckedChange = onDarkThemeChange,
        )
    }
}

@Composable
private fun PomodoroSection(
    pomodoroConfig: PomodoroConfig,
    onPomodoroWorkMinutes: (Int) -> Unit,
    onPomodoroBreakMinutes: (Int) -> Unit,
    onPomodoroSessions: (Int) -> Unit,
) {
    PomodoroConfigFields(
        config = pomodoroConfig,
        onWorkMinutes = onPomodoroWorkMinutes,
        onBreakMinutes = onPomodoroBreakMinutes,
        onSessions = onPomodoroSessions,
    )
}

@Composable
private fun FavoritesSection(
    rows: List<SettingsFavoriteRow>,
    appsLoaded: Boolean,
    onFavoriteChange: (componentKey: String, favorite: Boolean) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        when {
            !appsLoaded -> Unit
            rows.isEmpty() -> {
                item(key = "favorites_empty") {
                    Text(
                        text = stringResource(R.string.apps_installed_empty),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            else -> {
                items(
                    items = rows,
                    key = { it.app.componentKey },
                ) { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = row.app.label,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Switch(
                            checked = row.isFavorite,
                            onCheckedChange = { checked ->
                                onFavoriteChange(row.app.componentKey, checked)
                            },
                        )
                    }
                }
            }
        }
        item(key = "favorites_spacer") {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HabitsSection(
    habitRows: List<SettingsHabitRow>,
    onAddHabit: (label: String) -> Unit,
    onRemoveHabit: (habitId: String) -> Unit,
    onRenameHabit: (habitId: String, label: String) -> Unit,
) {
    var newHabitLabel by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item(key = "habits_add") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = newHabitLabel,
                    onValueChange = { newHabitLabel = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text(stringResource(R.string.habit_add_label)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val trimmed = newHabitLabel.trim()
                            if (trimmed.isNotEmpty()) {
                                onAddHabit(trimmed)
                                newHabitLabel = ""
                                focusManager.clearFocus()
                            }
                        },
                    ),
                )
                TextButton(
                    onClick = {
                        val trimmed = newHabitLabel.trim()
                        if (trimmed.isNotEmpty()) {
                            onAddHabit(trimmed)
                            newHabitLabel = ""
                            focusManager.clearFocus()
                        }
                    },
                ) {
                    Text(text = stringResource(R.string.habit_add_action))
                }
            }
        }

        if (habitRows.isEmpty()) {
            item(key = "habits_empty") {
                Text(
                    text = stringResource(R.string.habits_empty),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(
                items = habitRows,
                key = { it.habit.id },
            ) { row ->
                HabitEditRow(
                    row = row,
                    onRename = onRenameHabit,
                    onRemove = onRemoveHabit,
                )
            }
        }

        item(key = "habits_spacer") {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/** Spec 016 / RF-016-04 — permission rows + CTAs (state from ViewModel). */
@Composable
private fun PermissionsSection(
    status: PermissionsStatus,
    onOpenHomePicker: () -> Unit,
    onRequestCalendarPermission: () -> Unit,
    onRequestNotificationsPermission: () -> Unit,
    onOpenAppDetailsSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item(key = "perm_home") {
            PermissionRow(
                title = stringResource(R.string.settings_perm_home),
                statusText = if (status.isDefaultHome) {
                    stringResource(R.string.settings_perm_home_yes)
                } else {
                    stringResource(R.string.settings_perm_home_no)
                },
                primaryActionLabel = if (!status.isDefaultHome) {
                    stringResource(R.string.settings_perm_choose)
                } else {
                    null
                },
                onPrimaryAction = onOpenHomePicker,
            )
        }
        item(key = "perm_calendar") {
            PermissionRow(
                title = stringResource(R.string.settings_perm_calendar),
                statusText = if (status.calendarGranted) {
                    stringResource(R.string.settings_perm_granted)
                } else {
                    stringResource(R.string.settings_perm_denied)
                },
                primaryActionLabel = if (!status.calendarGranted) {
                    stringResource(R.string.settings_perm_allow)
                } else {
                    null
                },
                onPrimaryAction = onRequestCalendarPermission,
                secondaryActionLabel = if (!status.calendarGranted) {
                    stringResource(R.string.settings_perm_open_settings)
                } else {
                    null
                },
                onSecondaryAction = onOpenAppDetailsSettings,
            )
        }
        item(key = "perm_notifications") {
            PermissionRow(
                title = stringResource(R.string.settings_perm_notifications),
                statusText = if (status.notificationsGranted) {
                    stringResource(R.string.settings_perm_granted)
                } else {
                    stringResource(R.string.settings_perm_denied)
                },
                primaryActionLabel = if (!status.notificationsGranted) {
                    stringResource(R.string.settings_perm_allow)
                } else {
                    null
                },
                onPrimaryAction = onRequestNotificationsPermission,
                secondaryActionLabel = if (!status.notificationsGranted) {
                    stringResource(R.string.settings_perm_open_settings)
                } else {
                    null
                },
                onSecondaryAction = onOpenNotificationSettings,
            )
        }
        if (status.exactAlarmApplicable) {
            item(key = "perm_exact_alarm") {
                PermissionRow(
                    title = stringResource(R.string.settings_perm_exact_alarms),
                    statusText = if (status.exactAlarmGranted) {
                        stringResource(R.string.settings_perm_granted)
                    } else {
                        stringResource(R.string.settings_perm_denied)
                    },
                    primaryActionLabel = if (!status.exactAlarmGranted) {
                        stringResource(R.string.settings_perm_open_settings)
                    } else {
                        null
                    },
                    onPrimaryAction = onOpenExactAlarmSettings,
                )
            }
        }
        item(key = "perm_spacer") {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    statusText: String,
    primaryActionLabel: String? = null,
    onPrimaryAction: () -> Unit = {},
    secondaryActionLabel: String? = null,
    onSecondaryAction: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (primaryActionLabel != null || secondaryActionLabel != null) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (primaryActionLabel != null) {
                    TextButton(onClick = onPrimaryAction) {
                        Text(text = primaryActionLabel)
                    }
                }
                if (secondaryActionLabel != null) {
                    TextButton(onClick = onSecondaryAction) {
                        Text(text = secondaryActionLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun PomodoroConfigFields(
    config: PomodoroConfig,
    onWorkMinutes: (Int) -> Unit,
    onBreakMinutes: (Int) -> Unit,
    onSessions: (Int) -> Unit,
) {
    var workDraft by remember { mutableStateOf(config.workMinutes.toString()) }
    var breakDraft by remember { mutableStateOf(config.breakMinutes.toString()) }
    var sessionsDraft by remember { mutableStateOf(config.sessionsPerCycle.toString()) }
    LaunchedEffect(config.workMinutes, config.breakMinutes, config.sessionsPerCycle) {
        workDraft = config.workMinutes.toString()
        breakDraft = config.breakMinutes.toString()
        sessionsDraft = config.sessionsPerCycle.toString()
    }
    val focusManager = LocalFocusManager.current

    fun commitInt(raw: String, fallback: Int, onCommit: (Int) -> Unit) {
        val value = raw.toIntOrNull() ?: fallback
        onCommit(value)
        focusManager.clearFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = workDraft,
            onValueChange = { workDraft = it.filter { ch -> ch.isDigit() }.take(3) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.pomodoro_work_min)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { commitInt(workDraft, config.workMinutes, onWorkMinutes) },
            ),
        )
        OutlinedTextField(
            value = breakDraft,
            onValueChange = { breakDraft = it.filter { ch -> ch.isDigit() }.take(3) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.pomodoro_break_min)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { commitInt(breakDraft, config.breakMinutes, onBreakMinutes) },
            ),
        )
        OutlinedTextField(
            value = sessionsDraft,
            onValueChange = { sessionsDraft = it.filter { ch -> ch.isDigit() }.take(2) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(R.string.pomodoro_sessions)) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { commitInt(sessionsDraft, config.sessionsPerCycle, onSessions) },
            ),
        )
    }
}

@Composable
private fun HabitEditRow(
    row: SettingsHabitRow,
    onRename: (habitId: String, label: String) -> Unit,
    onRemove: (habitId: String) -> Unit,
) {
    val habit = row.habit
    var draft by remember(habit.id) { mutableStateOf(habit.label) }
    LaunchedEffect(habit.label) {
        draft = habit.label
    }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        val trimmed = draft.trim()
                        if (trimmed.isNotEmpty() && trimmed != habit.label) {
                            onRename(habit.id, trimmed)
                        } else {
                            draft = habit.label
                        }
                        focusManager.clearFocus()
                    },
                ),
            )
            TextButton(onClick = { onRemove(habit.id) }) {
                Text(text = stringResource(R.string.habit_delete_action))
            }
        }
        Row(
            modifier = Modifier.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (row.streak >= 1) {
                Text(
                    text = "· ${row.streak}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            row.lastSevenDays.forEach { done ->
                Text(
                    text = if (done) "●" else "○",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (done) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}
