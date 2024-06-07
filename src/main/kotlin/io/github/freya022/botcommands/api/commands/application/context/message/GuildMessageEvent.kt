package io.github.freya022.botcommands.api.commands.application.context.message

import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableInteractionImpl
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent

class GuildMessageEvent internal constructor(
    context: BContext,
    event: MessageContextInteractionEvent,
    cancellableRateLimit: CancellableRateLimit,
    localizableInteraction: LocalizableInteractionImpl
) : GlobalMessageEvent(context, event, cancellableRateLimit, localizableInteraction) {
    init {
        if (!event.isFromGuild)
            throwInternal("Event is not from a Guild")
    }

    /**
     * Always `true` for this guild-only event.
     */
    override fun isFromGuild(): Boolean {
        return true
    }

    /**
     * The [Member] who caused this interaction.
     *
     * Never null for this guild-only event.
     *
     * @return The [Member]
     */
    override fun getMember(): Member {
        return super.getMember()!!
    }

    /**
     * The [Guild] this interaction happened in.
     *
     * Never null for this guild-only event.
     *
     * @return The [Guild]
     */
    override fun getGuild(): Guild {
        return super.getGuild()!!
    }
}
