package io.github.freya022.botcommands.api.components.ratelimit

import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

internal class ComponentRateLimitingContext internal constructor(
    override val context: BContext,
    val event: GenericComponentInteractionCreateEvent,
    val rateLimitReference: ComponentRateLimitReference,
) : RateLimitingContext
