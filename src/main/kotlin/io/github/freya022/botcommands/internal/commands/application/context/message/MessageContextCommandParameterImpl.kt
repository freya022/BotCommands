package io.github.freya022.botcommands.internal.commands.application.context.message

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandResolverData
import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandOptionBuilder
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.message.MessageContextCommandParameter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.MessageContextParameterResolver
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.application.context.ContextCommandParameterImpl
import io.github.freya022.botcommands.internal.transform

internal class MessageContextCommandParameterImpl internal constructor(
    override val context: BContext,
    override val command: MessageCommandInfoImpl,
    builder: MessageCommandBuilder,
    optionAggregateBuilder: MessageCommandOptionAggregateBuilder
) : ContextCommandParameterImpl(context, optionAggregateBuilder, GlobalMessageEvent::class),
    MessageContextCommandParameter {

    override val nestedAggregatedParameters = optionAggregateBuilder.nestedAggregates.transform {
        MessageContextCommandParameterImpl(context, command, builder, it)
    }

    override val options = CommandOptions.transform<MessageCommandOptionBuilder, MessageContextParameterResolver<*, *>>(
        context,
        ApplicationCommandResolverData(builder),
        optionAggregateBuilder,
        optionFinalizer = { optionBuilder, resolver -> MessageContextCommandOptionImpl(context, command, optionBuilder, resolver) }
    )
}