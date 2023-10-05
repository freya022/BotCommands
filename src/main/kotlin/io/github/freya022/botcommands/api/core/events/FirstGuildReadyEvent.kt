package io.github.freya022.botcommands.api.core.events

import net.dv8tion.jda.api.events.guild.GuildReadyEvent

/**
 * Indicates that the first [GuildReadyEvent] was fired.
 *
 * @see InjectedJDAEvent
 */
class FirstGuildReadyEvent internal constructor(val event: GuildReadyEvent) : BEvent()