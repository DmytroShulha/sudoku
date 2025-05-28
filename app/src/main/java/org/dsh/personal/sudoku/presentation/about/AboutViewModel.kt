package org.dsh.personal.sudoku.presentation.about

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.dsh.personal.sudoku.utility.getAppVersionName

class AboutViewModel(application: Application) : ViewModel() {
    var showPrivacyPolicyDialog by mutableStateOf(false)
        private set

    val appVersionName = getAppVersionName(application)

    fun onPrivacyPolicyClick() {
        showPrivacyPolicyDialog = true
    }

    fun onDismissPrivacyPolicyDialog() {
        showPrivacyPolicyDialog = false
    }

    fun buildFeedbackEmailUri(appName: String, appVersion: String, feedbackEmail: String): String {
        return "mailto:$feedbackEmail?subject=Feedback%20for%20$appName%20v$appVersion"
    }


}