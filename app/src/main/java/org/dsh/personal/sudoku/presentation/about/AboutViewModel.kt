package org.dsh.personal.sudoku.presentation.about

import android.app.Application
import androidx.lifecycle.ViewModel
import org.dsh.personal.sudoku.utility.getAppVersionName

class AboutViewModel(application: Application) : ViewModel() {
    val appVersionName = getAppVersionName(application)
}