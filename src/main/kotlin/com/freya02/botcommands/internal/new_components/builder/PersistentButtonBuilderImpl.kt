package com.freya02.botcommands.internal.new_components.builder

import com.freya02.botcommands.api.new_components.builder.PersistentButtonBuilder
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.PersistentHandler
import com.freya02.botcommands.internal.new_components.new.ComponentController
import com.freya02.botcommands.internal.new_components.new.PersistentTimeout
import kotlinx.datetime.Clock
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import kotlin.time.Duration

internal class PersistentButtonBuilderImpl internal constructor(
    style: ButtonStyle,
    componentController: ComponentController
) : ButtonBuilderImpl<PersistentButtonBuilder>(componentController, style), PersistentButtonBuilder {
    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT

    override var timeout: PersistentTimeout? = null
        private set
    override var handler: PersistentHandler? = null
        private set

    override fun timeout(timeout: Duration, handlerName: String, vararg args: Any?): PersistentButtonBuilder = this.also {
        this.timeout = PersistentTimeout(Clock.System.now() + timeout, handlerName, args)
    }

    override fun bindTo(handlerName: String, vararg data: Any?): PersistentButtonBuilder =
        this.also { handler = PersistentHandler(handlerName, data) }
}