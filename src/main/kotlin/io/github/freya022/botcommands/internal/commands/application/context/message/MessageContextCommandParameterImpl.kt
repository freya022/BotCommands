package io.github.freya022.botcommands.internal.commands.application.context.message

import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.context.message.MessageContextCommandParameter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandResolverData
import io.github.freya022.botcommands.internal.commands.application.context.ContextCommandParameterImpl
import io.github.freya022.botcommands.internal.transform

internal class MessageContextCommandParameterImpl internal constructor(
    context: BContext,
    messageCommandInfo: MessageCommandInfoImpl,
    optionAggregateBuilder: MessageCommandOptionAggregateBuilder
) : ContextCommandParameterImpl(context, optionAggregateBuilder),
    MessageContextCommandParameter {

    override val nestedAggregatedParameters = optionAggregateBuilder.nestedAggregates.transform {
        MessageContextCommandParameterImpl(context, messageCommandInfo, it)
    }

    override val options = CommandOptions.transform(
        context,
        ApplicationCommandResolverData(messageCommandInfo),
        optionAggregateBuilder,
        optionFinalizer = ::MessageContextCommandOptionImpl
    )
}