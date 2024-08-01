package io.github.freya022.botcommands.internal.commands.application.context.message

import io.github.freya022.botcommands.api.commands.application.context.message.MessageContextCommandOption
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.MessageContextParameterResolver
import io.github.freya022.botcommands.internal.commands.application.context.ContextCommandOptionImpl
import io.github.freya022.botcommands.internal.commands.application.context.builder.MessageCommandOptionBuilderImpl

internal class MessageContextCommandOptionImpl internal constructor(
    override val context: BContext,
    override val command: MessageCommandInfoImpl,
    optionBuilder: MessageCommandOptionBuilderImpl,
    val resolver: MessageContextParameterResolver<*, *>
) : ContextCommandOptionImpl(optionBuilder),
    MessageContextCommandOption