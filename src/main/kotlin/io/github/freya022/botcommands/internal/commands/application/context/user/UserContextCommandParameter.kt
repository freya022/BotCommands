package io.github.freya022.botcommands.internal.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandOptionBuilder
import io.github.freya022.botcommands.api.parameters.UserContextParameterResolver
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.application.context.ContextCommandParameter
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.transform

class UserContextCommandParameter(
    context: BContextImpl,
    optionAggregateBuilder: UserCommandOptionAggregateBuilder
) : ContextCommandParameter(context, optionAggregateBuilder) {
    override val nestedAggregatedParameters = optionAggregateBuilder.nestedAggregates.transform {
        UserContextCommandParameter(context, it)
    }

    override val options = CommandOptions.transform(
        context,
        optionAggregateBuilder,
        object : CommandOptions.Configuration<UserCommandOptionBuilder, UserContextParameterResolver<*, *>> {
            override fun transformOption(
                optionBuilder: UserCommandOptionBuilder,
                resolver: UserContextParameterResolver<*, *>
            ) = UserContextCommandOption(optionBuilder, resolver)
        }
    )
}