package io.github.freya022.botcommands.api.core.events

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.guild.GuildReadyEvent

/**
 * Indicates that a JDA instance was acquired and injected in the [ServiceContainer].
 *
 * The JDA instance is not fully initialized, as this event is sent as fast as possible,
 * when the first JDA instance is retrievable.
 *
 * It is strongly discouraged to use [JDA.awaitReady] or any blocking method,
 * prefer using events, such as [GuildReadyEvent].
 *
 * **Note:** This event is only fired once.
 *
 * @see FirstGuildReadyEvent
 */
class InjectedJDAEvent internal constructor(context: BContext, val jda: JDA) : BEvent(context)