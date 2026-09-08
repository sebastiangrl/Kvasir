package app.kvasir.launcher.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.kvasir.launcher.R
import app.kvasir.launcher.domain.NextEventLineFormat
import app.kvasir.launcher.domain.model.InstalledApp
import app.kvasir.launcher.domain.model.NextCalendarEvent
import app.kvasir.launcher.ui.gesture.onVerticalSwipe
import app.kvasir.launcher.ui.icons.MonochromeAppIcon

/**
 * Spec 003–005 + 008 + 009 + Spec 010 / RF-010-01 + Spec 012 / RF-012-03, RF-012-05, RF-012-07 —
 * Home: clock, optional next-event line, search, favorites, habits.
 * No DataStore / PackageManager / LauncherApps / CalendarContract / StatusBar calls here.
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
    onFavoriteClick: (InstalledApp) -> Unit,
    onFavoriteLongClick: (InstalledApp) -> Unit,
    habitRows: List<HomeHabitRow>,
    onHabitCheckedChange: (habitId: String, completed: Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    onOpenOverlay: () -> Unit,
    onExpandSystemPanel: () -> Unit,
    nextEvent: NextCalendarEvent? = null,
    onNextEventClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .onVerticalSwipe(
                onSwipeUp = onOpenOverlay,
                onSwipeDown = onExpandSystemPanel,
            ),
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

            val eventLine = nextEvent?.let { NextEventLineFormat.format(it) }.orEmpty()
            if (eventLine.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = eventLine,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .clickable(onClick = onNextEventClick),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                singleLine = true,
                label = { Text(text = stringResource(R.string.search_apps_hint)) },
            )
            Spacer(modifier = Modifier.height(24.dp))

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
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = { onFavoriteClick(app) },
                                            onLongClick = { onFavoriteLongClick(app) },
                                        )
                                        .padding(horizontal = 24.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    MonochromeAppIcon(app = app)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = app.label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
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
