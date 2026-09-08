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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.kvasir.launcher.R
import app.kvasir.launcher.domain.model.InstalledApp

/**
 * Spec 001 + Spec 002 / RF-002-08, RF-002-09, RF-002-10 —
 * Home: isolated clock, empty favorites, CTA, provisional flat app list.
 * No PackageManager / LauncherApps calls here.
 */
@Composable
fun HomeScreen(
    showDefaultHomeCta: Boolean,
    onChooseHomeClick: () -> Unit,
    installedApps: List<InstalledApp>,
    appsLoaded: Boolean,
    onAppClick: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 32.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        HomeClock()
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = stringResource(R.string.home_empty_favorites),
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (showDefaultHomeCta) {
            Spacer(modifier = Modifier.height(24.dp))
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
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.apps_installed_header),
            modifier = Modifier.padding(horizontal = 24.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))

        when {
            !appsLoaded -> {
                // RF-002-10 — no fake rows while loading.
            }
            installedApps.isEmpty() -> {
                Text(
                    text = stringResource(R.string.apps_installed_empty),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.bodyMedium,
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
                        items = installedApps,
                        key = { it.componentKey },
                    ) { app ->
                        Text(
                            text = app.label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAppClick(app) }
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
