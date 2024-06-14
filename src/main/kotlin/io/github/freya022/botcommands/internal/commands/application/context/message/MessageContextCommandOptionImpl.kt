package io.github.freya022.botcommands.internal.commands.application.context.message

import io.github.freya022.botcommands.api.commands.application.context.builder.MessageCommandOptionBuilder
import io.github.freya022.botcommands.api.commands.application.context.message.MessageContextCommandOption
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.MessageContextParameterResolver
import io.github.freya022.botcommands.internal.commands.application.context.ContextCommandOptionImpl

internal class MessageContextCommandOptionImpl internal constructor(
    override val context: BContext,
    override val command: MessageCommandInfoImpl,
    optionBuilder: MessageCommandOptionBuilder,
    val resolver: MessageContextParameterResolver<*, *>
) : ContextCommandOptionImpl(optionBuilder),
    MessageContextCommandOption