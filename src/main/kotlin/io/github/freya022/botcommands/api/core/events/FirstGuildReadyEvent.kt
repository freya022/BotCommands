package io.github.freya022.botcommands.api.core.events

import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.guild.GuildReadyEvent

/**
 * Indicates that the first [GuildReadyEvent] was fired.
 *
 * @see InjectedJDAEvent
 */
class FirstGuildReadyEvent internal constructor(context: BContext, val event: GuildReadyEvent) : BEvent(context)