package io.github.freya022.botcommands.api.commands.application.context.message

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandFilter
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.localization.context.AppLocalizationContext
import io.github.freya022.botcommands.api.localization.interaction.*
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableInteractionImpl
import io.github.freya022.botcommands.internal.localization.interaction.LocalizableReplyCallbackImpl
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import java.util.*

/**
 * Interaction event for global message context commands.
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
 *
 * @see GuildMessageEvent
 */
open class GlobalMessageEvent internal constructor(
    val context: BContext,
    private val event: MessageContextInteractionEvent,
    cancellableRateLimit: CancellableRateLimit,
    private val localizableInteraction: LocalizableInteractionImpl
) : MessageContextInteractionEvent(event.jda, event.responseNumber, event.interaction),
    CancellableRateLimit by cancellableRateLimit,
    LocalizableInteraction by localizableInteraction,
    LocalizableReplyCallback by LocalizableReplyCallbackImpl(event.interaction, localizableInteraction) {

    override fun getHook(): LocalizableInteractionHook {
        return localizableInteraction.getHook()
    }

    override fun getRawData() = event.rawData
}