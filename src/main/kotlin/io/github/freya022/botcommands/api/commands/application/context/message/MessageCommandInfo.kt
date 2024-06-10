package io.github.freya022.botcommands.api.commands.application.context.message

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandInfo
import io.github.freya022.botcommands.internal.commands.application.context.message.MessageContextCommandParameter

interface MessageCommandInfo : TopLevelApplicationCommandInfo, ApplicationCommandInfo {
    override val parameters: List<MessageContextCommandParameter>
}