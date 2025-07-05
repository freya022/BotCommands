package io.github.freya022.botcommands.internal.commands.application.context.user.builder

import io.github.freya022.botcommands.api.commands.CommandType
import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.application.context.user.builder.UserCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.user.options.builder.UserCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.provider.AbstractApplicationCommandManager
import io.github.freya022.botcommands.internal.commands.application.builder.ApplicationCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.context.user.UserCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.context.user.options.builder.UserCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.builder.mixins.TopLevelApplicationCommandBuilderMixin
import io.github.freya022.botcommands.internal.commands.application.slash.builder.mixins.TopLevelApplicationCommandBuilderMixinImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

internal class UserCommandBuilderImpl internal constructor(
    manager: AbstractApplicationCommandManager,
    name: String,
    function: KFunction<Any>,
) : ApplicationCommandBuilderImpl<UserCommandOptionAggregateBuilder>(manager.context, name, function),
    UserCommandBuilder,
    TopLevelApplicationCommandBuilderMixin by TopLevelApplicationCommandBuilderMixinImpl(manager) {

    override val type: CommandType get() = CommandType.USER_CONTEXT
    override val topLevelBuilder get() = this
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