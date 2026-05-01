package io.github.freya022.botcommands.internal.commands.application.ratelimit

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent

internal class ApplicationCommandRateLimitingContext internal constructor(
    override val context: BContext,
    val event: GenericCommandInteractionEvent,
    val commandInfo: ApplicationCommandInfo,
) : RateLimitingContext
