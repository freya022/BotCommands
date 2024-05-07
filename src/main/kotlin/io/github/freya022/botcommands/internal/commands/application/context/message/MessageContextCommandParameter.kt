package io.github.freya022.botcommands.internal.commands.application.context.message

import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.MessageContextParameterResolver
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandResolverData
import io.github.freya022.botcommands.internal.commands.application.context.ContextCommandParameter
import io.github.freya022.botcommands.internal.transform

class MessageContextCommandParameter(
    context: BContext,
    messageCommandInfo: MessageCommandInfo,
    optionAggregateBuilder: MessageCommandOptionAggregateBuilder
) : ContextCommandParameter(context, optionAggregateBuilder) {
    override val nestedAggregatedParameters = optionAggregateBuilder.nestedAggregates.transform {
        MessageContextCommandParameter(context, messageCommandInfo, it)
    }

    override val options = CommandOptions.transform(
        context,
        ApplicationCommandResolverData(messageCommandInfo),
        optionAggregateBuilder,
        object : CommandOptions.Configuration<MessageCommandOptionBuilder, MessageContextParameterResolver<*, *>> {
            override fun transformOption(
                optionBuilder: MessageCommandOptionBuilder,
                resolver: MessageContextParameterResolver<*, *>
            ) = MessageContextCommandOption(optionBuilder, resolver)
        }
    )
}