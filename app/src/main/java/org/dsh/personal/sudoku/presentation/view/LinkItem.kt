package org.dsh.personal.sudoku.presentation.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp

@Stable
@Composable
fun LinkItem(
    icon: ImageVector,
    subicon: ImageVector,
    text: String,
    defaultElevation: Dp = Dimens.VerySmall,
    onClick: () -> Unit, // Ensure this lambda is stable when passed in
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.BigSmall),
        elevation = CardDefaults.cardElevation(defaultElevation = defaultElevation),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Dimens.Large, vertical = Dimens.BigMedium)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text, // Content description from text parameter
                modifier = Modifier.size(Dimens.Icon),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(Dimens.Large))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = subicon,
                contentDescription = null, // Decorative icon
                modifier = Modifier.size(Dimens.Icon),
                tint = MaterialTheme.colorScheme.surfaceTint
            )
        }
    }
}