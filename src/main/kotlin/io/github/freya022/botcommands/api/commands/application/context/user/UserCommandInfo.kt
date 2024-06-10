package io.github.freya022.botcommands.api.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandInfo
import io.github.freya022.botcommands.internal.commands.application.context.user.UserContextCommandParameter

interface UserCommandInfo : TopLevelApplicationCommandInfo, ApplicationCommandInfo {
    override val parameters: List<UserContextCommandParameter>
}