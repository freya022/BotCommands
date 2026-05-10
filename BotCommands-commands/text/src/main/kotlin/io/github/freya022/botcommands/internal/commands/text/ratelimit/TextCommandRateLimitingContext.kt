package io.github.freya022.botcommands.internal.commands.text.ratelimit

import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.ratelimit.RateLimitingContext
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

internal class TextCommandRateLimitingContext internal constructor(
    override val context: BContext,
    val event: MessageReceivedEvent,
    val commandInfo: TextCommandInfo,
) : RateLimitingContext
