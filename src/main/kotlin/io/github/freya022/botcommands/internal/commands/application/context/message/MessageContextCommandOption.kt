package io.github.freya022.botcommands.internal.commands.application.context.message

import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandOptionBuilder
import io.github.freya022.botcommands.api.parameters.resolvers.MessageContextParameterResolver
import io.github.freya022.botcommands.internal.commands.application.context.ContextCommandOption

class MessageContextCommandOption(
    optionBuilder: MessageCommandOptionBuilder,
    override val resolver: MessageContextParameterResolver<*, *>
) : ContextCommandOption(optionBuilder)