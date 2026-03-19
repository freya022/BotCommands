package io.github.freya022.botcommands.api.commands.ratelimit

import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

// TODO move to appropriate package once modularized
class TextCommandRateLimitingContext internal constructor(
    override val context: BContext,
    val event: MessageReceivedEvent,
    val commandInfo: TextCommandInfo,
) : RateLimitingContext
