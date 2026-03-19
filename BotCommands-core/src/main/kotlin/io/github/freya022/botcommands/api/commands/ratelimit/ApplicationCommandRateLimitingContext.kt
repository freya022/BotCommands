package io.github.freya022.botcommands.api.commands.ratelimit

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

// TODO move to appropriate package once modularized
class ApplicationCommandRateLimitingContext internal constructor(
    override val context: BContext,
    val event: GenericCommandInteractionEvent,
    val commandInfo: ApplicationCommandInfo,
) : RateLimitingContext
