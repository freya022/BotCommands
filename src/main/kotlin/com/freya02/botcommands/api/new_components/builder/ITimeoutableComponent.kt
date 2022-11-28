package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.internal.new_components.new.ComponentTimeout
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration
import kotlin.time.toDurationUnit

interface ITimeoutableComponent {
    val timeout: ComponentTimeout? //No need to use specific types in sub-interfaces as they're internal
}

@OptIn(ExperimentalTime::class)
interface IPersistentTimeoutableComponent<T : IPersistentTimeoutableComponent<T>> : ITimeoutableComponent {
    fun timeout(timeout: Long, timeoutUnit: TimeUnit, handlerName: String, vararg args: Any?): T =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit()), handlerName, *args)

    @JvmSynthetic
    fun timeout(timeout: Duration, handlerName: String, vararg args: Any?): T
}

@OptIn(ExperimentalTime::class)
interface IEphemeralTimeoutableComponent<T : IEphemeralTimeoutableComponent<T>> : ITimeoutableComponent {
    fun timeout(timeout: Long, timeoutUnit: TimeUnit, handler: Runnable): T =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit())) { runBlocking { handler.run() } }

    @JvmSynthetic
    fun timeout(timeout: Duration, handler: suspend () -> Unit): T
}
