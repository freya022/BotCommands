@file:Suppress("removal", "DEPRECATION")

package io.github.freya022.botcommands.internal.localization

import io.github.freya022.botcommands.api.localization.DefaultMessages
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import io.github.freya022.botcommands.api.localization.LocalizationService
import io.github.freya022.botcommands.api.localization.interaction.UserLocaleProvider
import io.github.freya022.botcommands.api.localization.text.TextCommandLocaleProvider
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

internal class FallbackDefaultMessagesFactory internal constructor(
    private val localizationService: LocalizationService,
    private val textCommandLocaleProvider: TextCommandLocaleProvider,
    private val userLocaleProvider: UserLocaleProvider,
): DefaultMessagesFactory {
    private val cache: MutableMap<Locale, DefaultMessages> = hashMapOf()

    override fun get(locale: Locale): DefaultMessages = cache.computeIfAbsent(locale) {
        DefaultMessages(localizationService, it)
    }

    override fun get(event: MessageReceivedEvent): DefaultMessages {
        return get(textCommandLocaleProvider.getLocale(event))
    }

    override fun get(event: Interaction): DefaultMessages {
        return get(userLocaleProvider.getLocale(event))
    }
}