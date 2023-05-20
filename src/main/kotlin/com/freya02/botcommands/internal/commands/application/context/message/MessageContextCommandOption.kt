package com.freya02.botcommands.internal.commands.application.context.message

import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandOptionBuilder
import com.freya02.botcommands.api.parameters.MessageContextParameterResolver
import com.freya02.botcommands.internal.commands.application.context.ContextCommandOption

class MessageContextCommandOption(
    optionBuilder: MessageCommandOptionBuilder,
    override val resolver: MessageContextParameterResolver<*, *>
) : ContextCommandOption(optionBuilder)