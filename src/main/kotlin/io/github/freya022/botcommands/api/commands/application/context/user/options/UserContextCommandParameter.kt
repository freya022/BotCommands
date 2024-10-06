package io.github.freya022.botcommands.api.commands.application.context.user.options

import io.github.freya022.botcommands.api.commands.application.context.options.ContextCommandParameter
import io.github.freya022.botcommands.api.commands.application.context.user.UserCommandInfo

/**
 * Represents a user context command parameter,
 * which has its value computed by an aggregation function, from one or more options.
 */
interface UserContextCommandParameter : ContextCommandParameter {
    override val nestedAggregatedParameters: List<UserContextCommandParameter>

    @Deprecated("Renamed to 'executable'", replaceWith = ReplaceWith("executable"))
    override val command: UserCommandInfo get() = executable
    override val executable: UserCommandInfo
}