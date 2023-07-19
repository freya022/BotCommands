package com.freya02.botcommands.api.core.events

import com.freya02.botcommands.api.core.service.ServiceContainer
import net.dv8tion.jda.api.JDA

/**
 * Indicates that a JDA instance was acquired and injected in the [ServiceContainer].
 *
 * @see FirstGuildReadyEvent
 */
class InjectedJDAEvent internal constructor(val jda: JDA) : BEvent()