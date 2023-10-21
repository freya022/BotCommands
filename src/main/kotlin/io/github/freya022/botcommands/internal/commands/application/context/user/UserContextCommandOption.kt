package io.github.freya022.botcommands.internal.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.context.builder.UserCommandOptionBuilder
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import io.github.freya022.botcommands.internal.commands.application.context.ContextCommandOption

class UserContextCommandOption(
    optionBuilder: UserCommandOptionBuilder,
    override val resolver: UserContextParameterResolver<*, *>
) : ContextCommandOption(optionBuilder)