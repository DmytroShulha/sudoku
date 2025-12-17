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
import androidx.compose.runtime.Immutable // Import for custom immutable classes
import androidx.compose.runtime.Stable // Import for marking stable classes/functions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import org.dsh.personal.sudoku.BuildConfig
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.presentation.view.Dimens
import org.dsh.personal.sudoku.theme.PersonalTheme
import java.time.Year

@Immutable
data class AboutScreenData(
    val appName: String,
    val appVersion: String,
    val playStoreGameUrl: String,
    val playStoreUrl: String,
    val githubRepo: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onHowToPlayClick: () -> Unit,
    params: AboutScreenData
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val appName = params.appName // Already efficient as it comes from params
    val versionText = stringResource(R.string.version_name, params.appVersion)
    val developedByText = stringResource(R.string.developed_by, BuildConfig.DEVELOPER_NAME)

    val currentYear = remember { Year.now().value }
    val copyrightText =
        stringResource(R.string.all_rights_reserved, currentYear, BuildConfig.DEVELOPER_NAME)
    val gameDescription = stringResource(R.string.game_description_short)
    val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)
    val aboutText = stringResource(R.string.about)
    val backText = stringResource(R.string.back)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(aboutText) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = backText,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
                .verticalScroll(rememberScrollState()), // Correctly uses rememberScrollState
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppHeaderSection(
                appName = appName, // Use the hoisted appName
                versionText = versionText,
                // Pass other required strings if they were hoisted and are specific here
            )

            DeveloperSection(developedByText = developedByText)

            DescriptionSection(gameDescription = gameDescription)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimens.Large),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color,
            )

            LinksSection(
                onHowToPlayClick = onHowToPlayClick,
                uriHandler = uriHandler, // Pass hoisted uriHandler
                context = context, // Pass hoisted context
                privacyPolicyUrl = privacyPolicyUrl,
                params = params // params is already stable (data class with vals)
            )

            Spacer(Modifier.weight(1f))

            CopyrightFooterSection(copyrightText = copyrightText)
        }
    }
}


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
    context: Context, // Added context to avoid LocalContext.current here
    privacyPolicyUrl: String,
    params: AboutScreenData,
) {
    val onHowToPlayClickStable by rememberUpdatedState(onHowToPlayClick)

    // For lambdas that capture changing values (like params or privacyPolicyUrl),
    // ensure they are stable or use remember with keys.
    val onOpenGithubRepo = remember(uriHandler, params.githubRepo, context) {
        { linkExecution(uriHandler, params.githubRepo, context) }
    }
    val onOpenPlayStore = remember(uriHandler, params.playStoreUrl, context) {
        { linkExecution(uriHandler, params.playStoreUrl, context) }
    }
    val onOpenFeedback = remember(uriHandler, params.playStoreGameUrl, context) {
        { linkExecution(uriHandler, params.playStoreGameUrl, context) }
    }
    val onOpenPrivacyPolicy = remember(uriHandler, privacyPolicyUrl, context) {
        { linkExecution(uriHandler, privacyPolicyUrl, context) }
    }
    // --- End of Lambda Optimization ---


    // Hoist string resources used multiple times or in loops if applicable
    val howToPlayText = stringResource(R.string.how_to_play_sudoku)
    val gitRepoText = stringResource(R.string.sudoku_git_repo)
    val moreGamesText = stringResource(R.string.more_games)
    val sendFeedbackText = stringResource(R.string.send_feedback)
    val privacyPolicyText = stringResource(R.string.privacy_policy)

    LinkItem(
        icon = Icons.AutoMirrored.Filled.HelpOutline,
        text = howToPlayText,
        onClick = onHowToPlayClickStable, // Use the stable lambda
        subicon = Icons.Filled.OpenInBrowser
    )

    LinkItem(
        icon = Icons.Filled.Gite,
        text = gitRepoText,
        onClick = onOpenGithubRepo,
        subicon = Icons.Filled.OpenInBrowser
    )

    LinkItem(
        icon = Icons.Filled.Explore,
        text = moreGamesText,
        onClick = onOpenPlayStore,
        subicon = Icons.Filled.OpenInBrowser
    )

    LinkItem(
        icon = Icons.Filled.Email,
        text = sendFeedbackText,
        onClick = onOpenFeedback,
        subicon = Icons.Filled.OpenInBrowser
    )

    LinkItem(
        icon = Icons.Filled.Policy,
        text = privacyPolicyText,
        onClick = onOpenPrivacyPolicy,
        subicon = Icons.Filled.OpenInBrowser
    )
}

private fun linkExecution(
    uriHandler: UriHandler, link: String, context: Context
) {
    Log.d("AboutScreen", "Attempting to open URI: $link")
    try {
        uriHandler.openUri(link)
    } catch (e: Exception) { // Catch broader Exception if other issues can occur
        Log.e("AboutScreen", "Error opening URI: $link", e)
        Toast.makeText(context, R.string.something_went_wrong, Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun DescriptionSection(gameDescription: String) {
    InfoItem(
        InfoItemData(
            icon = Icons.Filled.Description,
            label = stringResource(R.string.description), // Okay here, content changes with gameDescription
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
            label = stringResource(R.string.developer), // Okay here
            text = developedByText
        )
    )
}

@Composable
private fun AppHeaderSection(
    appName: String, // Receive appName as parameter
    versionText: String  // Receive versionText as parameter
) {
    // painterResource is generally efficient.
    // stringResource for contentDescription is fine as it's tied to appName.
    val appIconContentDescription = stringResource(R.string.app_icon, appName)

    Icon(
        painter = painterResource(R.drawable.ic_launcher_foreground),
        contentDescription = appIconContentDescription,
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

    // stringResource for "Version" label is acceptable here.
    InfoItem(
        InfoItemData(
            icon = Icons.Filled.Info,
            label = stringResource(R.string.version),
            text = versionText
        )
    )
}

@Immutable
data class InfoItemData(
    val icon: ImageVector,
    val label: String,
    val text: String,
    val modifier: Modifier = Modifier,
    val isMultiline: Boolean = false,
    val textAlign: TextAlign = TextAlign.Start,
)

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


@Preview(showBackground = true)
@Composable
fun AboutScreenPreviewLight() {
    // For previews, direct stringResource calls are generally fine.
    // The optimizations are more critical for runtime performance.
    PersonalTheme {
        AboutScreen(
            onNavigateBack = { },
            onHowToPlayClick = { },
            params = AboutScreenData(
                appName = "Sudoku Preview", // Use direct strings for preview if R cannot be resolved easily
                appVersion = "1.1.0-preview",
                playStoreUrl = "playStoreUrl_preview",
                githubRepo = "githubRepo_preview",
                playStoreGameUrl = "playStoreGameUrl_preview",
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
            onNavigateBack = { },
            onHowToPlayClick = { },
            params = AboutScreenData(
                appName = "Sudoku Preview Dark",
                appVersion = "1.1.0-preview-dark",
                playStoreUrl = "playStoreUrl_preview_dark",
                githubRepo = "githubRepo_preview_dark",
                playStoreGameUrl = "playStoreGameUrl_preview_dark",
            )
        )
    }
}
