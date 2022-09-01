package com.freya02.botcommands.api.commands.application.slash.builder.mixins

import com.freya02.botcommands.api.commands.application.CommandScope

interface ITopLevelApplicationCommandBuilder {
    val scope: CommandScope
    val isDefaultLocked: Boolean
    val isGuildOnly: Boolean
    val isTestOnly: Boolean
}
