package com.freya02.botcommands.api.application

import com.freya02.botcommands.internal.application.ApplicationCommandInfo

sealed interface IApplicationCommandManager {
    val guildApplicationCommands: List<ApplicationCommandInfo>
}