package app.kvasir.launcher.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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

/**
 * Spec 001 + Spec 003 / RF-003-03, RF-003-04, RF-003-06, RF-003-07 —
 * Home: clock, favorites (or empty), CTA, Settings entry.
 * No DataStore / PackageManager / LauncherApps calls here.
 */
@Composable
fun HomeScreen(
    showDefaultHomeCta: Boolean,
    onChooseHomeClick: () -> Unit,
    favorites: List<InstalledApp>,
    favoritesReady: Boolean,
    onFavoriteClick: (InstalledApp) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
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

        when {
            !favoritesReady -> {
                // Wait for installed-apps snapshot; do not invent rows (RF-003-04).
            }
            favorites.isEmpty() -> {
                Text(
                    text = stringResource(R.string.home_empty_favorites),
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
        }
    }
}
