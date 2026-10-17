package com.easylists.domain.common

interface Telemetry {
    fun setCrashReportingEnabled(enabled: Boolean)
    fun setAnalyticsEnabled(enabled: Boolean)
}
