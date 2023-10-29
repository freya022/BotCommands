package io.github.freya022.botcommands.api.core.events

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import net.dv8tion.jda.api.JDA

/**
 * Indicates that a JDA instance was acquired and injected in the [ServiceContainer].
 *
 * @see FirstGuildReadyEvent
 */
class InjectedJDAEvent internal constructor(context: BContext, val jda: JDA) : BEvent(context)