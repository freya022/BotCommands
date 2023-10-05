package io.github.freya022.botcommands.internal.commands.application.context.message

import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandOptionBuilder
import io.github.freya022.botcommands.api.parameters.MessageContextParameterResolver
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.application.context.ContextCommandParameter
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.transform

class MessageContextCommandParameter(
    context: BContextImpl,
    optionAggregateBuilder: MessageCommandOptionAggregateBuilder
) : ContextCommandParameter(context, optionAggregateBuilder) {
    override val nestedAggregatedParameters = optionAggregateBuilder.nestedAggregates.transform {
        MessageContextCommandParameter(context, it)
    }

    override val options = CommandOptions.transform(
        context,
        optionAggregateBuilder,
        object : CommandOptions.Configuration<MessageCommandOptionBuilder, MessageContextParameterResolver<*, *>> {
            override fun transformOption(
                optionBuilder: MessageCommandOptionBuilder,
                resolver: MessageContextParameterResolver<*, *>
            ) = MessageContextCommandOption(optionBuilder, resolver)
        }
    )
}