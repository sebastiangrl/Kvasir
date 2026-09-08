package app.kvasir.launcher.ui.settings

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
import androidx.compose.ui.unit.dp
import app.kvasir.launcher.R
import app.kvasir.launcher.domain.model.Habit

/**
 * Spec 003 + Spec 005 + Spec 006 / RF-006-03, RF-006-05 —
 * Favorites catalog + habit CRUD + theme Switch; no DataStore / LauncherApps here.
 */
@Composable
fun SettingsScreen(
    rows: List<SettingsFavoriteRow>,
    appsLoaded: Boolean,
    onFavoriteChange: (componentKey: String, favorite: Boolean) -> Unit,
    habits: List<Habit>,
    onAddHabit: (label: String) -> Unit,
    onRemoveHabit: (habitId: String) -> Unit,
    onRenameHabit: (habitId: String, label: String) -> Unit,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var newHabitLabel by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 8.dp),
    ) {
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
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item(key = "theme_row") {
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
                Spacer(modifier = Modifier.height(8.dp))
            }

            item(key = "favorites_header") {
                Text(
                    text = stringResource(R.string.choose_favorites),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            when {
                !appsLoaded -> {
                    // No fake rows while loading.
                }
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

            item(key = "habits_header") {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.habits_section),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

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

            if (habits.isEmpty()) {
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
                    items = habits,
                    key = { it.id },
                ) { habit ->
                    HabitEditRow(
                        habit = habit,
                        onRename = onRenameHabit,
                        onRemove = onRemoveHabit,
                    )
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HabitEditRow(
    habit: Habit,
    onRename: (habitId: String, label: String) -> Unit,
    onRemove: (habitId: String) -> Unit,
) {
    var draft by remember(habit.id) { mutableStateOf(habit.label) }
    LaunchedEffect(habit.label) {
        draft = habit.label
    }
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
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
}
