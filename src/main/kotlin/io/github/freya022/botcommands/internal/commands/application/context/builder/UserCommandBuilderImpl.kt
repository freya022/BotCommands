package io.github.freya022.botcommands.internal.commands.application.context.builder

import io.github.freya022.botcommands.api.commands.CommandType
import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.TopLevelApplicationCommandBuilderMixin
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.builder.ApplicationCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.context.user.UserCommandInfoImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

internal class UserCommandBuilderImpl internal constructor(
    context: BContext,
    name: String,
    function: KFunction<Any>,
    scope: CommandScope
) : ApplicationCommandBuilderImpl<UserCommandOptionAggregateBuilder>(context, name, function),
    UserCommandBuilder,
    ITopLevelApplicationCommandBuilder by TopLevelApplicationCommandBuilderMixin(scope) {

    override val type: CommandType = CommandType.USER_CONTEXT
    override val topLevelBuilder: ITopLevelApplicationCommandBuilder get() = this
    override val parentInstance: INamedCommand? get() = null

    override fun option(declaredName: String) {
        selfAggregate(declaredName) {
            option(declaredName)
        }
    }

    override fun constructAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>) =
        UserCommandOptionAggregateBuilderImpl(context, this, aggregatorParameter, aggregator)

    internal fun build(): UserCommandInfoImpl {
        return UserCommandInfoImpl(context, this)
    }
}