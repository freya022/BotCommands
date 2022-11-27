package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.*
import com.freya02.botcommands.internal.new_components.new.ComponentController
import com.freya02.botcommands.internal.new_components.new.ComponentTimeout
import com.freya02.botcommands.internal.new_components.new.EphemeralTimeout
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration
import kotlin.time.toDurationUnit

@OptIn(ExperimentalTime::class)
internal class EphemeralButtonBuilder internal constructor(
    style: ButtonStyle,
    componentController: ComponentController
) : ButtonBuilder<EphemeralButtonBuilder>(componentController, style) {
    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL

    //TODO use more accurate types in impl classes
    override var timeout: ComponentTimeout? = null
        private set
    override var handler: ComponentHandler? = null
        private set

    fun timeout(timeout: Long, timeoutUnit: TimeUnit, handler: Runnable): EphemeralButtonBuilder =
        timeout(timeout.toDuration(timeoutUnit.toDurationUnit())) { runBlocking { handler.run() } }

    @JvmSynthetic
    fun timeout(timeout: Duration, handler: suspend () -> Unit): EphemeralButtonBuilder = this.also {
        this.timeout = EphemeralTimeout(Clock.System.now() + timeout, handler)
    }

    fun bindTo(handler: (ButtonEvent) -> Unit): EphemeralButtonBuilder = this.also { it.handler = EphemeralHandler(handler) }
}