package io.github.freya022.botcommands.internal.commands.application.mixins

import io.github.freya022.botcommands.api.commands.application.CommandScope

interface ITopLevelApplicationCommandInfo {
    val scope: CommandScope
    val isDefaultLocked: Boolean
    val isGuildOnly: Boolean
}