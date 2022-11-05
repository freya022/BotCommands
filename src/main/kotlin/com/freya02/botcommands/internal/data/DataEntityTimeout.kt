package com.freya02.botcommands.internal.data

import kotlinx.datetime.Clock
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration
import kotlin.time.toDurationUnit

data class DataEntityTimeout(val duration: Duration, val handlerName: String?) {
    @OptIn(ExperimentalTime::class)
    constructor(timeout: Long, timeoutUnit: TimeUnit, handlerName: String?) : this(timeout.toDuration(timeoutUnit.toDurationUnit()), handlerName)

    fun toDateEntityExpiration() = DataEntityExpiration(duration.let { Clock.System.now().plus(duration) }, handlerName)
}