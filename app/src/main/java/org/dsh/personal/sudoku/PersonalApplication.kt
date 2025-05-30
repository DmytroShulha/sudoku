package org.dsh.personal.sudoku

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class PersonalApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PersonalApplication)

            modules(
                appModule, sudokuDi
            )
        }
    }
}
