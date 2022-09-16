package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.commands.CommandPath

sealed interface Cooldownable {
    val path: CommandPath
    val cooldownStrategy: CooldownStrategy
}