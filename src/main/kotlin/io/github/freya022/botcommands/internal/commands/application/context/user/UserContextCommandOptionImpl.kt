package io.github.freya022.botcommands.internal.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.context.user.UserContextCommandOption
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import io.github.freya022.botcommands.internal.commands.application.context.ContextCommandOptionImpl
import io.github.freya022.botcommands.internal.commands.application.context.builder.UserCommandOptionBuilderImpl

internal class UserContextCommandOptionImpl internal constructor(
    override val context: BContext,
    override val command: UserCommandInfoImpl,
    optionBuilder: UserCommandOptionBuilderImpl,
    val resolver: UserContextParameterResolver<*, *>
) : ContextCommandOptionImpl(optionBuilder),
    UserContextCommandOption