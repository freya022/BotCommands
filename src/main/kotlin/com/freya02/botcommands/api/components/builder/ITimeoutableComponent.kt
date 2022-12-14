package com.freya02.botcommands.api.components.builder

import com.freya02.botcommands.internal.components.data.ComponentTimeout
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration
import kotlin.time.toDurationUnit

@OptIn(ExperimentalTime::class)
interface ITimeoutableComponent {
    val timeout: ComponentTimeout? //No need to use specific types in sub-interfaces as they're internal

    fun timeout(timeout: Long, timeoutUnit: TimeUnit) =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit()))

    fun timeout(timeout: Duration)
}

@OptIn(ExperimentalTime::class)
interface IPersistentTimeoutableComponent : ITimeoutableComponent {
    fun timeout(timeout: Long, timeoutUnit: TimeUnit, handlerName: String, vararg args: Any?) =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit()), handlerName, *args)

    @JvmSynthetic
    fun timeout(timeout: Duration, handlerName: String, vararg args: Any?)
}

@OptIn(ExperimentalTime::class)
interface IEphemeralTimeoutableComponent : ITimeoutableComponent {
    //TODO (docs) warn about captured jda entities
    fun timeout(timeout: Long, timeoutUnit: TimeUnit, handler: Runnable) =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit())) { runBlocking { handler.run() } }

    @JvmSynthetic
    fun timeout(timeout: Duration, handler: suspend () -> Unit)
}
