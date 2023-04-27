package com.freya02.botcommands.internal.commands.application.context.message

import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandOptionBuilder
import com.freya02.botcommands.api.parameters.MessageContextParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.CommandOptions
import com.freya02.botcommands.internal.commands.application.context.ContextCommandParameter

class MessageContextCommandParameter(
    context: BContextImpl,
    optionAggregateBuilder: MessageCommandOptionAggregateBuilder
) : ContextCommandParameter(context, optionAggregateBuilder) {
    val commandOptions = CommandOptions.transform(
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