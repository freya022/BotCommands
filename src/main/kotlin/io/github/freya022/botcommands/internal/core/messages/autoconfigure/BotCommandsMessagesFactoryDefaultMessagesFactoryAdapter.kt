@file:Suppress("removal", "DEPRECATION")

package io.github.freya022.botcommands.internal.core.messages.autoconfigure

import io.github.freya022.botcommands.api.core.messages.BotCommandsMessages
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

internal class BotCommandsMessagesFactoryDefaultMessagesFactoryAdapter internal constructor(
    private val defaultMessagesFactory: DefaultMessagesFactory,
) : BotCommandsMessagesFactory {

    override fun get(locale: Locale): BotCommandsMessages {
        return BotCommandsMessagesDefaultMessagesAdapter(defaultMessagesFactory.get(locale))
    }

    override fun get(event: MessageReceivedEvent): BotCommandsMessages {
        return BotCommandsMessagesDefaultMessagesAdapter(defaultMessagesFactory.get(event))
    }

    override fun get(event: Interaction): BotCommandsMessages {
        return BotCommandsMessagesDefaultMessagesAdapter(defaultMessagesFactory.get(event))
    }
}
