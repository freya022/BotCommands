package io.github.freya022.botcommands.internal.core.replies

import io.github.freya022.botcommands.api.core.replies.BuiltinReplies
import io.github.freya022.botcommands.api.core.replies.BuiltinRepliesFactory
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.Interaction
import java.util.*

internal class BuiltinRepliesFactoryDefaultMessagesFactoryAdapter internal constructor(
    private val defaultMessagesFactory: DefaultMessagesFactory,
) : BuiltinRepliesFactory {

    override fun get(locale: Locale): BuiltinReplies {
        return BuiltinRepliesDefaultMessagesAdapter(defaultMessagesFactory.get(locale))
    }

    override fun get(event: MessageReceivedEvent): BuiltinReplies {
        return BuiltinRepliesDefaultMessagesAdapter(defaultMessagesFactory.get(event))
    }

    override fun get(event: Interaction): BuiltinReplies {
        return BuiltinRepliesDefaultMessagesAdapter(defaultMessagesFactory.get(event))
    }
}