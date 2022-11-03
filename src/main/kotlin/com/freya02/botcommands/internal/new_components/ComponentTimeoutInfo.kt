package com.freya02.botcommands.internal.new_components

import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration
import kotlin.time.toDurationUnit

class ComponentTimeoutInfo(val timeout: Long, val timeoutUnit: TimeUnit, val timeoutHandlerName: String?) {
    @OptIn(ExperimentalTime::class)
    fun asDuration(): Duration = timeout.toDuration(timeoutUnit.toDurationUnit())
}