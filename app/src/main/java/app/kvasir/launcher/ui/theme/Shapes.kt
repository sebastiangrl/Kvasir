package app.kvasir.launcher.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Spec 019 / RF-019-03 — pill / round controls (search + habit checks).
 */
val KvasirPillShape = RoundedCornerShape(percent = 50)

@Composable
fun KvasirRoundCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fill = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (checked) fill else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (checked) fill else outline,
                shape = CircleShape,
            )
            .clickable(role = Role.Checkbox) { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Text(
                text = "✓",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
