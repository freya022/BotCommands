package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.new_components.builder.EphemeralButtonBuilder
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.EphemeralHandler
import com.freya02.botcommands.internal.new_components.new.ComponentController
import com.freya02.botcommands.internal.new_components.new.EphemeralTimeout
import kotlinx.datetime.Clock
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import kotlin.time.Duration

internal class EphemeralButtonBuilderImpl internal constructor(
    style: ButtonStyle,
    componentController: ComponentController
) : ButtonBuilderImpl<EphemeralButtonBuilder>(componentController, style), EphemeralButtonBuilder {
    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL

    override var timeout: EphemeralTimeout? = null
        private set
    override var handler: EphemeralHandler<*>? = null
        private set

    @JvmSynthetic
    override fun timeout(timeout: Duration, handler: suspend () -> Unit): EphemeralButtonBuilder = this.also {
        this.timeout = EphemeralTimeout(Clock.System.now() + timeout, handler)
    }

    override fun bindTo(handler: (ButtonEvent) -> Unit): EphemeralButtonBuilder = this.also { it.handler = EphemeralHandler(handler) }
}