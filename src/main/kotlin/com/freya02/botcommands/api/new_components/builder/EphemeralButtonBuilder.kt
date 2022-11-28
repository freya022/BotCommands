package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.internal.new_components.*
import com.freya02.botcommands.internal.new_components.new.EphemeralTimeout
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration
import kotlin.time.toDurationUnit

@OptIn(ExperimentalTime::class)
interface EphemeralButtonBuilder : ButtonBuilder<EphemeralButtonBuilder> {
    override val timeout: EphemeralTimeout?
    override val handler: EphemeralHandler<*>?

    fun timeout(timeout: Long, timeoutUnit: TimeUnit, handler: Runnable): EphemeralButtonBuilder =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit())) { runBlocking { handler.run() } }

    @JvmSynthetic
    fun timeout(timeout: Duration, handler: suspend () -> Unit): EphemeralButtonBuilder

    //TODO suspend & java
    fun bindTo(handler: (ButtonEvent) -> Unit): EphemeralButtonBuilder
}