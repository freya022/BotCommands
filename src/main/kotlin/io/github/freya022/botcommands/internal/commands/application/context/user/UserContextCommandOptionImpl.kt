package io.github.freya022.botcommands.internal.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandOptionBuilder
import io.github.freya022.botcommands.api.commands.application.context.user.UserContextCommandOption
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import io.github.freya022.botcommands.internal.commands.application.context.ContextCommandOptionImpl

internal class UserContextCommandOptionImpl internal constructor(
    optionBuilder: UserCommandOptionBuilder,
    val resolver: UserContextParameterResolver<*, *>
) : ContextCommandOptionImpl(optionBuilder),
    UserContextCommandOption