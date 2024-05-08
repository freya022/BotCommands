package io.github.freya022.botcommands.internal.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandResolverData
import io.github.freya022.botcommands.internal.commands.application.context.ContextCommandParameter
import io.github.freya022.botcommands.internal.transform

class UserContextCommandParameter(
    context: BContext,
    userCommandInfo: UserCommandInfo,
    optionAggregateBuilder: UserCommandOptionAggregateBuilder
) : ContextCommandParameter(context, optionAggregateBuilder) {
    override val nestedAggregatedParameters = optionAggregateBuilder.nestedAggregates.transform {
        UserContextCommandParameter(context, userCommandInfo, it)
    }

    override val options = CommandOptions.transform(
        context,
        ApplicationCommandResolverData(userCommandInfo),
        optionAggregateBuilder,
        object : CommandOptions.Configuration<UserCommandOptionBuilder, UserContextParameterResolver<*, *>> {
            override fun transformOption(
                optionBuilder: UserCommandOptionBuilder,
                resolver: UserContextParameterResolver<*, *>
            ) = UserContextCommandOption(optionBuilder, resolver)
        }
    )
}