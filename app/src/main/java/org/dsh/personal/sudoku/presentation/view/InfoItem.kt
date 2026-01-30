package org.dsh.personal.sudoku.presentation.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import org.dsh.personal.sudoku.presentation.about.InfoItemData

@Stable
@Composable
fun InfoItem(
    data: InfoItemData, // data is now an @Immutable data class instance
) {
    Row(
        verticalAlignment = if (data.isMultiline) Alignment.Top else Alignment.CenterVertically,
        modifier = data.modifier // data.modifier comes from the InfoItemData which should be stable
            .fillMaxWidth()
            .padding(vertical = Dimens.Medium)
            .padding(end = Dimens.Medium)
    ) {
        Icon(
            imageVector = data.icon,
            contentDescription = data.label, // Content description comes from potentially dynamic data
            modifier = Modifier.size(Dimens.Icon),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.width(Dimens.Large))
        Column {
            Text(
                text = data.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = data.text,
                style = getStyle(data), // getStyle is a simple conditional, efficient
                textAlign = data.textAlign,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
@Stable
@Composable
private fun getStyle(data: InfoItemData): TextStyle =
    if (data.isMultiline)
        MaterialTheme.typography.bodyMedium
    else
        MaterialTheme.typography.bodyLarge
