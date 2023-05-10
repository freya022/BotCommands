package com.freya02.botcommands.internal.commands.application.context.user

import com.freya02.botcommands.api.commands.application.context.builder.UserCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandOptionBuilder
import com.freya02.botcommands.api.parameters.UserContextParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.CommandOptions
import com.freya02.botcommands.internal.commands.application.context.ContextCommandParameter

class UserContextCommandParameter(
    context: BContextImpl,
    optionAggregateBuilder: UserCommandOptionAggregateBuilder
) : ContextCommandParameter(context, optionAggregateBuilder) {
    override val commandOptions = CommandOptions.transform(
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