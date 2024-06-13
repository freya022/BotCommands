package io.github.freya022.botcommands.internal.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.context.user.UserContextCommandParameter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandResolverData
import io.github.freya022.botcommands.internal.commands.application.context.ContextCommandParameterImpl
import io.github.freya022.botcommands.internal.transform

internal class UserContextCommandParameterImpl internal constructor(
    context: BContext,
    userCommandInfo: UserCommandInfoImpl,
    optionAggregateBuilder: UserCommandOptionAggregateBuilder,
) : ContextCommandParameterImpl(context, optionAggregateBuilder),
    UserContextCommandParameter {

    override val nestedAggregatedParameters = optionAggregateBuilder.nestedAggregates.transform {
        UserContextCommandParameterImpl(context, userCommandInfo, it)
    }

    override val options = CommandOptions.transform(
        context,
        ApplicationCommandResolverData(userCommandInfo),
        optionAggregateBuilder,
        optionFinalizer = ::UserContextCommandOptionImpl
    )
}