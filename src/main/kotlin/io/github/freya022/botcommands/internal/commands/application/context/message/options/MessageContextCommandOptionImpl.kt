package io.github.freya022.botcommands.internal.commands.application.context.message.options

import io.github.freya022.botcommands.api.commands.application.context.message.options.MessageContextCommandOption
import io.github.freya022.botcommands.api.parameters.resolvers.MessageContextParameterResolver
import io.github.freya022.botcommands.internal.commands.application.context.message.MessageCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.context.message.options.builder.MessageCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.context.options.ContextCommandOptionImpl

internal class MessageContextCommandOptionImpl internal constructor(
    override val executable: MessageCommandInfoImpl,
    optionBuilder: MessageCommandOptionBuilderImpl,
    val resolver: MessageContextParameterResolver<*, *>
) : ContextCommandOptionImpl(optionBuilder),
    MessageContextCommandOption