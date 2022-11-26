package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.internal.new_components.new.ComponentTimeout
import com.freya02.botcommands.internal.new_components.new.EphemeralTimeout
import com.freya02.botcommands.internal.new_components.new.PersistentTimeout
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration
import kotlin.time.toDurationUnit

@OptIn(ExperimentalTime::class)
class ComponentGroupBuilder internal constructor(@get:JvmSynthetic internal val componentIds: List<Int>) {
    @get:JvmSynthetic
    internal var oneUse: Boolean = false
    @get:JvmSynthetic
    internal var timeout: ComponentTimeout? = null

    fun oneUse() = this.also { oneUse = true }

    fun setTimeout(timeout: Long, timeoutUnit: TimeUnit, handler: Runnable): ComponentGroupBuilder =
        setTimeout(timeout.toDuration(timeoutUnit.toDurationUnit())) {
            runBlocking { handler.run() }
        }

    @JvmSynthetic
    fun setTimeout(timeout: Duration, handler: suspend () -> Unit): ComponentGroupBuilder = this.also {
        this.timeout = EphemeralTimeout(Clock.System.now() + timeout, handler)
    }

    fun setTimeout(timeout: Long, timeoutUnit: TimeUnit, handlerName: String, vararg args: Any?): ComponentGroupBuilder =
        setTimeout(timeout.toDuration(timeoutUnit.toDurationUnit()), handlerName, *args)

    @JvmSynthetic
    fun setTimeout(timeout: Duration, handlerName: String, vararg args: Any?): ComponentGroupBuilder = this.also {
        this.timeout = PersistentTimeout(Clock.System.now() + timeout, handlerName, args)
    }
}