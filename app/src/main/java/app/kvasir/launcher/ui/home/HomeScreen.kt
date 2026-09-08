package app.kvasir.launcher.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import app.kvasir.launcher.ui.gesture.onVerticalSwipe

/**
 * Spec 001 + Spec 003 + Spec 004 / RF-004-01 —
 * Home: clock, favorites, CTA, Settings; swipe-up opens apps overlay.
 */
@Composable
fun HomeScreen(
    showDefaultHomeCta: Boolean,
    onChooseHomeClick: () -> Unit,
    favorites: List<InstalledApp>,
    favoritesReady: Boolean,
    onFavoriteClick: (InstalledApp) -> Unit,
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

            when {
                !favoritesReady -> {
                    Spacer(modifier = Modifier.weight(1f))
                }
                favorites.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.home_empty_favorites),
                        modifier = Modifier.padding(horizontal = 24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
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
}
