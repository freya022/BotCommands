package io.github.freya022.botcommands.internal.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandOptionBuilder
import io.github.freya022.botcommands.api.commands.application.context.user.UserContextCommandParameter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandResolverData
import io.github.freya022.botcommands.internal.commands.application.context.ContextCommandParameterImpl
import io.github.freya022.botcommands.internal.transform

internal class UserContextCommandParameterImpl internal constructor(
    override val context: BContext,
    override val command: UserCommandInfoImpl,
    optionAggregateBuilder: UserCommandOptionAggregateBuilder,
) : ContextCommandParameterImpl(context, optionAggregateBuilder),
    UserContextCommandParameter {

    override val nestedAggregatedParameters = optionAggregateBuilder.nestedAggregates.transform {
        UserContextCommandParameterImpl(context, command, it)
    }

    override val options = CommandOptions.transform<UserCommandOptionBuilder, UserContextParameterResolver<*, *>>(
        context,
        ApplicationCommandResolverData(command),
        optionAggregateBuilder,
        optionFinalizer = { optionBuilder, resolver -> UserContextCommandOptionImpl(context, command, optionBuilder, resolver) }
    )
}