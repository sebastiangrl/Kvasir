package app.kvasir.launcher.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.kvasir.launcher.R

/**
 * Spec 001 / RF-001-06, RF-001-07, RF-001-08 —
 * Home: isolated clock + empty favorites + optional default-Home CTA.
 * No PackageManager / LauncherApps calls here.
 */
@Composable
fun HomeScreen(
    showDefaultHomeCta: Boolean,
    onChooseHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(vertical = 32.dp),
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
    }
}
