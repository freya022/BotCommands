package io.github.freya022.botcommands.api.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.context.ContextCommandOption

interface UserContextCommandOption : ContextCommandOption {
    override val command: UserCommandInfo
}