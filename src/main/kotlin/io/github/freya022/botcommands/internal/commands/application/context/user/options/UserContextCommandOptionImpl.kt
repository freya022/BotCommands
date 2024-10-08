package io.github.freya022.botcommands.internal.commands.application.context.user.options

import io.github.freya022.botcommands.api.commands.application.context.user.options.UserContextCommandOption
import io.github.freya022.botcommands.api.commands.application.context.user.options.UserContextCommandParameter
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import io.github.freya022.botcommands.internal.commands.application.context.options.ContextCommandOptionImpl
import io.github.freya022.botcommands.internal.commands.application.context.user.options.builder.UserCommandOptionBuilderImpl

internal class UserContextCommandOptionImpl internal constructor(
    override val parent: UserContextCommandParameter,
    optionBuilder: UserCommandOptionBuilderImpl,
    val resolver: UserContextParameterResolver<*, *>
) : ContextCommandOptionImpl(optionBuilder),
    UserContextCommandOption