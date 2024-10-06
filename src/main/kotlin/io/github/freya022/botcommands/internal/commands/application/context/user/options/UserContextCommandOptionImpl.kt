package io.github.freya022.botcommands.internal.commands.application.context.user.options

import io.github.freya022.botcommands.api.commands.application.context.user.options.UserContextCommandOption
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.UserContextParameterResolver
import io.github.freya022.botcommands.internal.commands.application.context.options.ContextCommandOptionImpl
import io.github.freya022.botcommands.internal.commands.application.context.user.UserCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.context.user.options.builder.UserCommandOptionBuilderImpl

internal class UserContextCommandOptionImpl internal constructor(
    override val context: BContext,
    override val executable: UserCommandInfoImpl,
    optionBuilder: UserCommandOptionBuilderImpl,
    val resolver: UserContextParameterResolver<*, *>
) : ContextCommandOptionImpl(optionBuilder),
    UserContextCommandOption