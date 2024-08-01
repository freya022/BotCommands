package io.github.freya022.botcommands.internal.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandResolverData
import io.github.freya022.botcommands.api.commands.application.context.user.GlobalUserEvent
import io.github.freya022.botcommands.api.commands.application.context.user.UserContextCommandParameter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.application.context.ContextCommandParameterImpl
import io.github.freya022.botcommands.internal.commands.application.context.builder.UserCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.context.builder.UserCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.context.builder.UserCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.transform

internal class UserContextCommandParameterImpl internal constructor(
    override val context: BContext,
    override val command: UserCommandInfoImpl,
    builder: UserCommandBuilderImpl,
    optionAggregateBuilder: UserCommandOptionAggregateBuilderImpl
) : ContextCommandParameterImpl(context, optionAggregateBuilder, GlobalUserEvent::class),
    UserContextCommandParameter {

    override val nestedAggregatedParameters = optionAggregateBuilder.optionAggregateBuilders.transform {
        UserContextCommandParameterImpl(context, command, builder, it as UserCommandOptionAggregateBuilderImpl)
    }

    override val options = CommandOptions.transform<UserCommandOptionBuilderImpl, UserContextParameterResolver<*, *>>(
        context,
        ApplicationCommandResolverData(builder),
        optionAggregateBuilder,
        optionFinalizer = { optionBuilder, resolver -> UserContextCommandOptionImpl(context, command, optionBuilder, resolver) }
    )
}