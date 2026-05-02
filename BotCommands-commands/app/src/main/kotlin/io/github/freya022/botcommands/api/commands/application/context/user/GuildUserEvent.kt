package io.github.freya022.botcommands.api.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.interaction.GuildLocaleProvider
import io.github.freya022.botcommands.api.localization.interaction.LocalizableInteraction
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableInteractionImpl
import io.github.freya022.botcommands.internal.utils.throwInternal
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import java.util.*

/**
 * Interaction event for guild user context commands.
 *
 * This event is guaranteed to be run in guilds only,
 * thus getters of guild entities will never return `null`.
 *
 * ### Localization
 * You can send localized replies using the user, guild and also any [Locale],
 * by using this event directly, but also the interaction hook,
 * see [LocalizableInteraction] for more details and configuration.
 *
 * An alternative to using this event is injecting an [AppLocalizationContext] in a parameter,
 * or retrieving one using [getLocalizationContext].
 *
 * In both cases, you can configure the user and guild locales,
 * using [UserLocaleProvider] and [GuildLocaleProvider].
 *
 * ### Rate limit cancellation
 * Although it is recommended to reject commands using [ApplicationCommandFilter],
 * you can also return the bucket token with [cancelRateLimit]
 * if you want to avoid consuming bandwidth in certain conditions.
 */
class GuildUserEvent internal constructor(
    context: BContext,
    event: UserContextInteractionEvent,
    cancellableRateLimit: CancellableRateLimit,
    localizableInteraction: LocalizableInteractionImpl
) : GlobalUserEvent(context, event, cancellableRateLimit, localizableInteraction) {
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
