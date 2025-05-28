package org.dsh.personal.sudoku.utility

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.dsh.personal.sudoku.R

/**
 * Retrieves the application version name.
 *
 * @return The version name.
 */
fun getAppVersionName(context: Context, default: String = "N/A"): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo?.versionName ?: default
    } catch (_: PackageManager.NameNotFoundException) {
        default
    }
}