package com.easylists.domain.models

data class SyncSummary(val pulled: Int, val reconciled: Int, val pushed: Int)
