package io.github.freya022.botcommands.internal.commands.application.context.message

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandResolverData
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.message.MessageContextCommandParameter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.MessageContextParameterResolver
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.application.context.ContextCommandParameterImpl
import io.github.freya022.botcommands.internal.commands.application.context.builder.MessageCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.context.builder.MessageCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.context.builder.MessageCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.transform

internal class MessageContextCommandParameterImpl internal constructor(
    override val context: BContext,
    override val command: MessageCommandInfoImpl,
    builder: MessageCommandBuilderImpl,
    optionAggregateBuilder: MessageCommandOptionAggregateBuilderImpl
) : ContextCommandParameterImpl(context, optionAggregateBuilder, GlobalMessageEvent::class),
    MessageContextCommandParameter {

    override val nestedAggregatedParameters = optionAggregateBuilder.optionAggregateBuilders.transform {
        MessageContextCommandParameterImpl(context, command, builder, it as MessageCommandOptionAggregateBuilderImpl)
    }

    override val options = CommandOptions.transform<MessageCommandOptionBuilderImpl, MessageContextParameterResolver<*, *>>(
        context,
        ApplicationCommandResolverData(builder),
        optionAggregateBuilder,
        optionFinalizer = { optionBuilder, resolver -> MessageContextCommandOptionImpl(context, command, optionBuilder, resolver) }
    )
}