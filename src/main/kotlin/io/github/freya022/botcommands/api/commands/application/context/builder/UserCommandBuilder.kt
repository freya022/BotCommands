package io.github.freya022.botcommands.api.commands.application.context.builder

import io.github.freya022.botcommands.api.commands.CommandType
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.TopLevelApplicationCommandBuilderMixin
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.context.user.UserCommandInfo
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

class UserCommandBuilder internal constructor(
    context: BContext,
    name: String,
    function: KFunction<Any>,
    scope: CommandScope
) : ApplicationCommandBuilder<UserCommandOptionAggregateBuilder>(context, name, function),
    ITopLevelApplicationCommandBuilder by TopLevelApplicationCommandBuilderMixin(scope) {

    override val type: CommandType = CommandType.USER_CONTEXT
    override val topLevelBuilder: ITopLevelApplicationCommandBuilder = this
    override val parentInstance: INamedCommand? = null

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun option(declaredName: String) {
        selfAggregate(declaredName) {
            option(declaredName)
        }
    }

    override fun constructAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>) =
        UserCommandOptionAggregateBuilder(aggregatorParameter, aggregator)

    internal fun build(): UserCommandInfo {
        return UserCommandInfo(context, this)
    }
}
