package com.easylists

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.easylists.data.sync.SyncTriggers
import com.easylists.domain.common.Telemetry
import com.easylists.domain.models.SyncScheduler
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
    @Inject lateinit var syncTriggers: SyncTriggers
    @Inject lateinit var syncScheduler: SyncScheduler

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

        // A sync a few seconds after every local edit
        syncTriggers.start(applicationScope)

        // And one every time the app comes to the foreground
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                when (event) {
                    // Coming back: sync, unless one finished in the last 30 seconds
                    Lifecycle.Event.ON_START -> syncScheduler.requestSync(minIntervalSeconds = 30)
                    // Leaving: send anything that is still waiting
                    Lifecycle.Event.ON_STOP -> syncTriggers.flushIfWaiting(applicationScope)
                    else -> Unit
                }
            }
        )

    }

}
