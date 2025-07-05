package io.github.freya022.botcommands.internal.commands.application.context.message.builder

import io.github.freya022.botcommands.api.commands.CommandType
import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.application.context.message.builder.MessageCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.message.options.builder.MessageCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.provider.AbstractApplicationCommandManager
import io.github.freya022.botcommands.internal.commands.application.builder.ApplicationCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.context.message.MessageCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.context.message.options.builder.MessageCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.builder.mixins.TopLevelApplicationCommandBuilderMixin
import io.github.freya022.botcommands.internal.commands.application.slash.builder.mixins.TopLevelApplicationCommandBuilderMixinImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

internal class MessageCommandBuilderImpl internal constructor(
    manager: AbstractApplicationCommandManager,
    name: String,
    function: KFunction<Any>,
) : ApplicationCommandBuilderImpl<MessageCommandOptionAggregateBuilder>(manager.context, name, function),
    MessageCommandBuilder,
    TopLevelApplicationCommandBuilderMixin by TopLevelApplicationCommandBuilderMixinImpl(manager) {

    override val type: CommandType get() = CommandType.MESSAGE_CONTEXT
    override val topLevelBuilder get() = this
    override val parentInstance: INamedCommand? get() = null

    override fun option(declaredName: String) {
        selfAggregate(declaredName) {
            option(declaredName)
        }
    }

    override fun constructAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>) =
        MessageCommandOptionAggregateBuilderImpl(context, this, aggregatorParameter, aggregator)

    internal fun build(): MessageCommandInfoImpl {
        return MessageCommandInfoImpl(context, this)
    }
}