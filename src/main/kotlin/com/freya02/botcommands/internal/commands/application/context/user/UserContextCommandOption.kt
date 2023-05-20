package com.freya02.botcommands.internal.commands.application.context.user

import com.freya02.botcommands.api.commands.application.context.builder.UserCommandOptionBuilder
import com.freya02.botcommands.api.parameters.UserContextParameterResolver
import com.freya02.botcommands.internal.commands.application.context.ContextCommandOption

class UserContextCommandOption(
    optionBuilder: UserCommandOptionBuilder,
    override val resolver: UserContextParameterResolver<*, *>
) : ContextCommandOption(optionBuilder)