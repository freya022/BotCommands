package io.github.freya022.botcommands.api.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.context.ContextCommandParameter

/**
 * Represents a user context command parameter,
 * which has its value computed by an aggregation function, from one or more options.
 */
interface UserContextCommandParameter : ContextCommandParameter {
    override val nestedAggregatedParameters: List<UserContextCommandParameter>

    override val command: UserCommandInfo
}