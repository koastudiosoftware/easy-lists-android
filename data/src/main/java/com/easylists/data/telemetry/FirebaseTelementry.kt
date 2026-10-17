package com.easylists.data.telemetry

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.easylists.domain.common.Telemetry
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named

class FirebaseTelemetry @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("isDebugBuild") private val isDebugBuild: Boolean,
) : Telemetry {

    override fun setCrashReportingEnabled(enabled: Boolean) {
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = enabled && !isDebugBuild
    }

    @RequiresPermission(allOf = [
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.WAKE_LOCK
    ])
    override fun setAnalyticsEnabled(enabled: Boolean) {
        FirebaseAnalytics.getInstance(context)
            .setAnalyticsCollectionEnabled(enabled && !isDebugBuild)
    }
}