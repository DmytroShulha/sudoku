package org.dsh.personal.sudoku.presentation.about

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import org.dsh.personal.sudoku.BuildConfig.DEVELOPER_NAME
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.presentation.view.Dimens
import org.dsh.personal.sudoku.theme.PersonalTheme
import java.time.Year


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onHowToPlayClick: () -> Unit,
    appName: String,
    appVersion: String,
    playStoreUrl: String,
    githubRepo: String,
) {
    val uriHandler = LocalUriHandler.current

    val versionText = stringResource(R.string.version_name, appVersion)
    val developedByText = stringResource(R.string.developed_by, DEVELOPER_NAME)
    val copyrightText = stringResource(R.string.all_rights_reserved, Year.now().value, DEVELOPER_NAME)
    val gameDescription = stringResource(R.string.game_description_short)
    val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Dimens.Large)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // FR1.1 App Icon
            // FR1.2 Game Title
            // FR1.3 Version Number
            AppHeaderSection(appName, versionText)

            // FR2.1 Developer Information
            DeveloperSection(developedByText)

            // FR3.1 Brief Description
            DescriptionSection(gameDescription)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimens.Large),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color,
            )

            // FR4.1 How to Play
            // FR4.2 Feedback/Support
            // FR4.3 Privacy Policy
            LinksSection(onHowToPlayClick, uriHandler, privacyPolicyUrl, playStoreUrl, githubRepo)

            Spacer(Modifier.weight(1f)) // Pushes copyright to the bottom

            // FR2.2 Copyright Information
            CopyrightFooterSection(copyrightText)
        }
    }
}

@Composable
private fun CopyrightFooterSection(copyrightText: String) {
    InfoItem(
        icon = Icons.Filled.Copyright,
        label = stringResource(R.string.copyright),
        text = copyrightText,
        textAlign = TextAlign.Start,
        modifier = Modifier.padding(top = Dimens.Large, bottom = Dimens.Medium)
    )
}

@Composable
private fun LinksSection(
    onHowToPlayClick: () -> Unit,
    uriHandler: UriHandler,
    privacyPolicyUrl: String,
    playStoreUrl: String,
    githubRepo: String,
) {
    val context = LocalContext.current

    LinkItem(
        icon = Icons.AutoMirrored.Filled.HelpOutline,
        text = stringResource(R.string.how_to_play_sudoku),
        onClick = onHowToPlayClick,
        subicon = Icons.Filled.OpenInBrowser
    )

    LinkItem(
        icon = Icons.Filled.Gite,
        text = stringResource(R.string.sudoku_git_repo),
        onClick = {
            try {
                uriHandler.openUri(githubRepo)
            } catch (e: Exception) {
                Log.e("AboutScreen", "Error opening URI", e)
                Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
            }
        },
        subicon = Icons.Filled.OpenInBrowser
    )

    LinkItem(
        icon = Icons.Filled.Email,
        text = stringResource(R.string.send_feedback),
        onClick = {
            try {
                uriHandler.openUri(playStoreUrl)
            } catch (e: Exception) {
                Log.e("AboutScreen", "Error opening URI", e)
                Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
            }
        },
        subicon = Icons.Filled.OpenInBrowser
    )

    LinkItem(
        icon = Icons.Filled.Policy,
        text = stringResource(R.string.privacy_policy),
        onClick = {
            try {
                uriHandler.openUri(privacyPolicyUrl)
            } catch (e: Exception) {
                Log.e("AboutScreen", "Error opening URI", e)
                Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
            }
        },
        subicon = Icons.Filled.OpenInBrowser
    )
}

@Composable
private fun DescriptionSection(gameDescription: String) {
    InfoItem(
        icon = Icons.Filled.Description,
        label = stringResource(R.string.description),
        text = gameDescription,
        isMultiline = true
    )
}

@Composable
private fun DeveloperSection(developedByText: String) {
    InfoItem(
        icon = Icons.Filled.Business,
        label = stringResource(R.string.developer),
        text = developedByText
    )
}

@Composable
private fun AppHeaderSection(appName: String, versionText: String) {

    Icon(
        painter = painterResource(R.drawable.ic_launcher_foreground),
        contentDescription = stringResource(R.string.app_icon, appName),
        modifier = Modifier
            .size(Dimens.Image)
            .padding(bottom = Dimens.Large),
        tint = MaterialTheme.colorScheme.primary
    )

    Text(
        text = appName,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = Dimens.Small)
    )

    InfoItem(icon = Icons.Filled.Info, label = stringResource(R.string.version), text = versionText)
}

@Composable
fun InfoItem(
    icon: ImageVector,
    label: String,
    text: String,
    modifier: Modifier = Modifier,
    isMultiline: Boolean = false,
    textAlign: TextAlign = TextAlign.Start,
) {
    Row(
        verticalAlignment = if (isMultiline) Alignment.Top else Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.Medium)
            .padding(end = Dimens.Medium)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(Dimens.Icon),
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.width(Dimens.Large))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = text,
                style = if (isMultiline) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                textAlign = textAlign,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun LinkItem(
    icon: ImageVector,
    subicon: ImageVector,
    text: String,
    defaultElevation: Dp = Dimens.VerySmall,
    onClick: () -> Unit,
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
            modifier = Modifier
                .padding(horizontal = Dimens.Large, vertical = Dimens.BigMedium)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
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
                contentDescription = null,
                modifier = Modifier.size(Dimens.Icon),
                tint = MaterialTheme.colorScheme.surfaceTint
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AboutScreenPreviewLight() {
    PersonalTheme {
        AboutScreen(
            onNavigateBack = { },
            onHowToPlayClick = { },
            appName = stringResource(R.string.app_name),
            appVersion = "1.1.0",
            playStoreUrl = "",
            githubRepo = "githubRepo",
        )
    }
}

@Preview(showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
            or android.content.res.Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun AboutScreenPreviewDark() {
    PersonalTheme {
        AboutScreen(
            onNavigateBack = { },
            onHowToPlayClick = { },
            appName = stringResource(R.string.app_name),
            appVersion = "1.1.0",
            playStoreUrl = "",
            githubRepo = "",
        )
    }
}
