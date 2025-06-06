package org.dsh.personal.sudoku.presentation.about

import android.content.Context
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
import androidx.compose.material.icons.filled.Explore
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
import androidx.compose.ui.text.TextStyle
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
    onNavigateBack: () -> Unit, onHowToPlayClick: () -> Unit, params: AboutScreenData
) {
    val uriHandler = LocalUriHandler.current

    val versionText = stringResource(R.string.version_name, params.appVersion)
    val developedByText = stringResource(R.string.developed_by, DEVELOPER_NAME)
    val copyrightText =
        stringResource(R.string.all_rights_reserved, Year.now().value, DEVELOPER_NAME)
    val gameDescription = stringResource(R.string.game_description_short)
    val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about)) }, navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
            )
        }) { paddingValues ->
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
            AppHeaderSection(params.appName, versionText)

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
            LinksSection(
                onHowToPlayClick = onHowToPlayClick,
                uriHandler = uriHandler,
                privacyPolicyUrl = privacyPolicyUrl,
                params = params
            )

            Spacer(Modifier.weight(1f)) // Pushes copyright to the bottom

            // FR2.2 Copyright Information
            CopyrightFooterSection(copyrightText)
        }
    }
}

data class AboutScreenData(
    val appName: String,
    val appVersion: String,
    val playStoreGameUrl: String,
    val playStoreUrl: String,
    val githubRepo: String,
)

@Composable
private fun CopyrightFooterSection(copyrightText: String) {
    InfoItem(
        InfoItemData(
            icon = Icons.Filled.Copyright,
            label = stringResource(R.string.copyright),
            text = copyrightText,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(top = Dimens.Large, bottom = Dimens.Medium)
        )
    )
}

@Composable
private fun LinksSection(
    onHowToPlayClick: () -> Unit,
    uriHandler: UriHandler,
    privacyPolicyUrl: String,
    params: AboutScreenData,
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
        onClick = { linkExecution(uriHandler, params.githubRepo, context) },
        subicon = Icons.Filled.OpenInBrowser
    )

    LinkItem(
        icon = Icons.Filled.Explore,
        text = stringResource(R.string.more_games),
        onClick = { linkExecution(uriHandler, params.playStoreUrl, context) },
        subicon = Icons.Filled.OpenInBrowser
    )

    LinkItem(
        icon = Icons.Filled.Email,
        text = stringResource(R.string.send_feedback),
        onClick = { linkExecution(uriHandler, params.playStoreGameUrl, context) },
        subicon = Icons.Filled.OpenInBrowser
    )

    LinkItem(
        icon = Icons.Filled.Policy,
        text = stringResource(R.string.privacy_policy),
        onClick = { linkExecution(uriHandler, privacyPolicyUrl, context) },
        subicon = Icons.Filled.OpenInBrowser
    )
}

private fun linkExecution(
    uriHandler: UriHandler, link: String, context: Context
) {
    try {
        uriHandler.openUri(link)
    } catch (e: IllegalArgumentException) {
        Log.e("AboutScreen", "Error opening URI", e)
        Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun DescriptionSection(gameDescription: String) {
    InfoItem(
        InfoItemData(
            icon = Icons.Filled.Description,
            label = stringResource(R.string.description),
            text = gameDescription,
            isMultiline = true
        )
    )
}

@Composable
private fun DeveloperSection(developedByText: String) {
    InfoItem(
        InfoItemData(
            icon = Icons.Filled.Business,
            label = stringResource(R.string.developer),
            text = developedByText
        )
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

    InfoItem(
        InfoItemData(
            icon = Icons.Filled.Info, label = stringResource(R.string.version), text = versionText
        )
    )
}

data class InfoItemData(
    val icon: ImageVector,
    val label: String,
    val text: String,
    val modifier: Modifier = Modifier,
    val isMultiline: Boolean = false,
    val textAlign: TextAlign = TextAlign.Start,
)

@Composable
fun InfoItem(
    data: InfoItemData,
) {
    Row(
        verticalAlignment = if (data.isMultiline) Alignment.Top else Alignment.CenterVertically,
        modifier = data.modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.Medium)
            .padding(end = Dimens.Medium)
    ) {
        Icon(
            imageVector = data.icon,
            contentDescription = data.label,
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
                style = getStyle(data),
                textAlign = data.textAlign,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun getStyle(data: InfoItemData): TextStyle =
    if (data.isMultiline)
        MaterialTheme.typography.bodyMedium
    else
        MaterialTheme.typography.bodyLarge

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
            modifier = Modifier.padding(horizontal = Dimens.Large, vertical = Dimens.BigMedium)
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
            onNavigateBack = { }, onHowToPlayClick = { }, params = AboutScreenData(
            appName = stringResource(R.string.app_name),
            appVersion = "1.1.0",
            playStoreUrl = "",
            githubRepo = "githubRepo",
            playStoreGameUrl = "",
        )
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
            or android.content.res.Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun AboutScreenPreviewDark() {
    PersonalTheme {
        AboutScreen(
            onNavigateBack = { }, onHowToPlayClick = { }, params = AboutScreenData(
            appName = stringResource(R.string.app_name),
            appVersion = "1.1.0",
            playStoreUrl = "",
            githubRepo = "",
            playStoreGameUrl = "",
        )
        )
    }
}
