package io.github.freya022.botcommands.api.commands.ratelimit

import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitReference
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

// TODO move to appropriate package once modularized
class ComponentRateLimitingContext internal constructor(
    override val context: BContext,
    val event: GenericComponentInteractionCreateEvent,
    val rateLimitReference: ComponentRateLimitReference,
) : RateLimitingContext
