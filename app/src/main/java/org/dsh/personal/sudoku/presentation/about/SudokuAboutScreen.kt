package org.dsh.personal.sudoku.presentation.about

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Gite
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dsh.personal.sudoku.BuildConfig
import org.dsh.personal.sudoku.R
import org.dsh.personal.sudoku.presentation.view.Dimens
import org.dsh.personal.sudoku.presentation.view.InfoItem
import org.dsh.personal.sudoku.presentation.view.LinkItem
import org.dsh.personal.sudoku.theme.PersonalTheme

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
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val appName = params.appName
    val versionText = stringResource(R.string.version_name, params.appVersion)
    val developedByText = stringResource(R.string.developed_by, BuildConfig.DEVELOPER_NAME)

    val gameDescription = stringResource(R.string.game_description_short)
    val privacyPolicyUrl = stringResource(R.string.privacy_policy_url)
    val aboutText = stringResource(R.string.about)
    val backText = stringResource(R.string.back)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        aboutText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = backText,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Dimens.Large)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.Large)
        ) {
            AppHeaderSection(
                appName = appName,
                versionText = versionText,
            )

            AppInformationCard(
                developedByText = developedByText,
                gameDescription = gameDescription
            )

            LinksSection(
                onHowToPlayClick = onHowToPlayClick,
                uriHandler = uriHandler,
                context = context,
                snackbarHostState = snackbarHostState,
                scope = scope,
                privacyPolicyUrl = privacyPolicyUrl,
                params = params
            )
        }
    }
}

@Composable
private fun LinksSection(
    onHowToPlayClick: () -> Unit,
    uriHandler: UriHandler,
    context: Context,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    privacyPolicyUrl: String,
    params: AboutScreenData,
) {
    val onHowToPlayClickStable by rememberUpdatedState(onHowToPlayClick)

    val onOpenGithubRepo = remember(uriHandler, params.githubRepo, context) {
        { linkExecution(context, uriHandler, params.githubRepo, snackbarHostState, scope) }
    }
    val onOpenPlayStore = remember(uriHandler, params.playStoreUrl, context) {
        { linkExecution(context, uriHandler, params.playStoreUrl, snackbarHostState, scope) }
    }
    val onOpenPrivacyPolicy = remember(uriHandler, privacyPolicyUrl, context) {
        { linkExecution(context, uriHandler, privacyPolicyUrl, snackbarHostState, scope) }
    }
    val onRateApp = remember(uriHandler, params.playStoreGameUrl, context) {
        { linkExecution(context, uriHandler, params.playStoreGameUrl, snackbarHostState, scope) }
    }
    val onShareApp = remember(context, params.playStoreGameUrl) {
        { shareApp(context, params.playStoreGameUrl, params.appName) }
    }

    val howToPlayText = stringResource(R.string.how_to_play_sudoku)
    val gitRepoText = stringResource(R.string.sudoku_git_repo)
    val moreGamesText = stringResource(R.string.more_games)
    val privacyPolicyText = stringResource(R.string.privacy_policy)
    val rateAppText = stringResource(R.string.rate_app)
    val shareAppText = stringResource(R.string.share_app)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.Small)
    ) {
        Text(
            stringResource(R.string.links),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = Dimens.Small)
        )

        LinkItem(
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            text = howToPlayText,
            onClick = onHowToPlayClickStable,
            subicon = Icons.Filled.OpenInBrowser
        )

        LinkItem(
            icon = Icons.Filled.Android,
            text = rateAppText,
            onClick = onRateApp,
            subicon = Icons.Filled.OpenInBrowser
        )

        LinkItem(
            icon = Icons.Filled.Share,
            text = shareAppText,
            onClick = onShareApp,
            subicon = Icons.Filled.Share
        )

        LinkItem(
            icon = Icons.Filled.Gite,
            text = gitRepoText,
            onClick = onOpenGithubRepo,
            subicon = Icons.Filled.OpenInBrowser
        )

        LinkItem(
            icon = Icons.Filled.Apps,
            text = moreGamesText,
            onClick = onOpenPlayStore,
            subicon = Icons.Filled.OpenInBrowser
        )

        LinkItem(
            icon = Icons.Filled.Policy,
            text = privacyPolicyText,
            onClick = onOpenPrivacyPolicy,
            subicon = Icons.Filled.OpenInBrowser
        )
    }
}

private fun linkExecution(
    context: Context,
    uriHandler: UriHandler,
    link: String,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    Log.d("AboutScreen", "Attempting to open URI: $link")
    try {
        uriHandler.openUri(link)
    } catch (e: IllegalArgumentException) {
        Log.e("AboutScreen", "Error opening URI: $link", e)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = context.getString(R.string.could_not_open_link),
                actionLabel = context.getString(R.string.retry),
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                linkExecution(context, uriHandler, link, snackbarHostState, scope)
            }
        }
    }
}

private fun shareApp(context: Context, appUrl: String, appName: String) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, appName)
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_app_text, appName, appUrl))
    }
    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_app_chooser_title, appName)))
}

@Composable
private fun AppInformationCard(
    developedByText: String,
    gameDescription: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens.Large),
            verticalArrangement = Arrangement.spacedBy(Dimens.Medium)
        ) {
            Text(
                stringResource(R.string.app_information),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            InfoItem(
                InfoItemData(
                    icon = Icons.Filled.Business,
                    label = stringResource(R.string.developer),
                    text = developedByText
                )
            )

            InfoItem(
                InfoItemData(
                    icon = Icons.Filled.Description,
                    label = stringResource(R.string.description),
                    text = gameDescription,
                    isMultiline = true
                )
            )
        }
    }
}

@Composable
private fun AppHeaderSection(
    appName: String,
    versionText: String
) {
    val appIconContentDescription = stringResource(R.string.app_icon, appName)

    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = appIconContentDescription,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(modifier = Modifier.size(Dimens.Medium))

    Text(
        text = appName,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
    )

    Text(
        text = versionText,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
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


@Preview(showBackground = true)
@Composable
fun AboutScreenPreviewLight() {
    PersonalTheme {
        AboutScreen(
            onNavigateBack = { },
            onHowToPlayClick = { },
            params = AboutScreenData(
                appName = "Personal Sudoku",
                appVersion = "1.0.0",
                playStoreUrl = "https://play.google.com/store",
                githubRepo = "https://github.com/example/sudoku",
                playStoreGameUrl = "https://play.google.com/store/apps/details?id=com.example.sudoku",
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
    PersonalTheme(darkTheme = true) {
        AboutScreen(
            onNavigateBack = { },
            onHowToPlayClick = { },
            params = AboutScreenData(
                appName = "Personal Sudoku",
                appVersion = "1.0.0",
                playStoreUrl = "https://play.google.com/store",
                githubRepo = "https://github.com/example/sudoku",
                playStoreGameUrl = "https://play.google.com/store/apps/details?id=com.example.sudoku",
            )
        )
    }
}
