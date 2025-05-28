package org.dsh.personal.sudoku.presentation.about

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.presentation.view.Dimens
import org.dsh.personal.sudoku.theme.PersonalTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyDialog(
    appName: String,
    contactEmail: String,
    onDismissRequest: () -> Unit
) {
    val privacyPolicyText =
        stringResource(R.string.privacy_policy_text, appName, appName, appName, contactEmail).trimIndent()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(stringResource(R.string.privacy_policy))
        },
        text = {
            Text(
                text = privacyPolicyText,
                modifier = Modifier
                    .padding(vertical = Dimens.Medium)
                    .verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest
            ) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewPrivacyPolicyDialog() {
    PersonalTheme {
        PrivacyPolicyDialog(
            onDismissRequest = {},
            appName = stringResource(R.string.app_name),
            contactEmail = stringResource(R.string.sudoku_feedback_email),
        )
    }
}