package app.kvasir.launcher.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.kvasir.launcher.R
import app.kvasir.launcher.domain.model.InstalledApp
import app.kvasir.launcher.ui.gesture.onVerticalSwipe

/**
 * Spec 003 + 004 + Spec 005 / RF-005-02, RF-005-03, RF-005-06, RF-005-07 —
 * Home: clock (isolated), favorites, habits with Checkbox; swipe-up opens overlay.
 * No DataStore / PackageManager / LauncherApps calls here.
 */
@Composable
fun HomeScreen(
    showDefaultHomeCta: Boolean,
    onChooseHomeClick: () -> Unit,
    favorites: List<InstalledApp>,
    favoritesReady: Boolean,
    onFavoriteClick: (InstalledApp) -> Unit,
    habitRows: List<HomeHabitRow>,
    onHabitCheckedChange: (habitId: String, completed: Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    onOpenOverlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .onVerticalSwipe(onSwipeUp = onOpenOverlay),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            TextButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 8.dp),
            ) {
                Text(text = stringResource(R.string.settings))
            }

            HomeClock()
            Spacer(modifier = Modifier.height(48.dp))

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
                    if (favorites.isEmpty()) {
                        item(key = "favorites_empty") {
                            Text(
                                text = stringResource(R.string.home_empty_favorites),
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        items(
                            items = favorites,
                            key = { it.componentKey },
                        ) { app ->
                            Text(
                                text = app.label,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onFavoriteClick(app) }
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
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
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = row.completed,
                                onCheckedChange = { checked ->
                                    onHabitCheckedChange(row.habit.id, checked)
                                },
                            )
                            Text(
                                text = row.habit.label,
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
}
