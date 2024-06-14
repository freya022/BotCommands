package io.github.freya022.botcommands.api.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.context.ContextCommandParameter

interface UserContextCommandParameter : ContextCommandParameter {
    override val nestedAggregatedParameters: List<UserContextCommandParameter>

    override val command: UserCommandInfo
}