package io.github.freya022.botcommands.api.commands.application

interface TopLevelApplicationCommandInfo {
    val scope: CommandScope
    val isDefaultLocked: Boolean
    val isGuildOnly: Boolean
    val nsfw: Boolean
}