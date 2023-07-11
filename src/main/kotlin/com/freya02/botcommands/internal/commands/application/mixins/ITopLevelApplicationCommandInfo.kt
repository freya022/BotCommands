package com.freya02.botcommands.internal.commands.application.mixins

import com.freya02.botcommands.api.commands.application.CommandScope

interface ITopLevelApplicationCommandInfo {
    val scope: CommandScope
    val isDefaultLocked: Boolean
    val isGuildOnly: Boolean
}