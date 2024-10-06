package io.github.freya022.botcommands.internal.commands.application.context.message.options

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandResolverData
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.message.options.MessageContextCommandParameter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.MessageContextParameterResolver
import io.github.freya022.botcommands.internal.commands.application.context.message.MessageCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.context.message.builder.MessageCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.context.message.options.builder.MessageCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.context.message.options.builder.MessageCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.context.options.ContextCommandParameterImpl
import io.github.freya022.botcommands.internal.options.CommandOptions
import io.github.freya022.botcommands.internal.options.transform

internal class MessageContextCommandParameterImpl internal constructor(
    context: BContext,
    override val executable: MessageCommandInfoImpl,
    builder: MessageCommandBuilderImpl,
    optionAggregateBuilder: MessageCommandOptionAggregateBuilderImpl
) : ContextCommandParameterImpl(context, optionAggregateBuilder, GlobalMessageEvent::class),
    MessageContextCommandParameter {

    override val nestedAggregatedParameters = optionAggregateBuilder.optionAggregateBuilders.transform {
        MessageContextCommandParameterImpl(context, executable, builder, it as MessageCommandOptionAggregateBuilderImpl)
    }

    override val options = CommandOptions.transform<MessageCommandOptionBuilderImpl, MessageContextParameterResolver<*, *>>(
        context,
        executable,
        ApplicationCommandResolverData(builder),
        optionAggregateBuilder,
        optionFinalizer = { optionBuilder, resolver -> MessageContextCommandOptionImpl(executable, optionBuilder, resolver) }
    )
}