package com.easylists

import android.app.Application
import com.toxicbakery.logging.Arbor
import com.toxicbakery.logging.Seedling
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Arbor.sow(Seedling())
//        PrefsHelper.init(applicationContext)
    }

}