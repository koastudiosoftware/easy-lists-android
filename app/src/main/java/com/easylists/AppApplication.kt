package com.easylists

import android.app.Application
import com.easylists.domain.common.Telemetry
import com.easylists.domain.repositories.SettingsRepository
import com.toxicbakery.logging.Arbor
import com.toxicbakery.logging.Seedling
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AppApplication : Application() {

    @Inject
    lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var telemetry: Telemetry

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        Arbor.sow(Seedling())

        applicationScope.launch {
            settingsRepository.settings
                .map { it.sendCrashReports to it.sendAnalytics }
                .distinctUntilChanged()
                .collect { (crashReports, analytics) ->
                    telemetry.setCrashReportingEnabled(crashReports)
                    telemetry.setAnalyticsEnabled(analytics)
                }
        }
    }

}