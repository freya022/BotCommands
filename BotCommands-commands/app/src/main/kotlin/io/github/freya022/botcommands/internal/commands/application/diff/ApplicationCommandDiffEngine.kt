package io.github.freya022.botcommands.internal.commands.application.diff

internal interface ApplicationCommandDiffEngine {
    context(_: DiffLogger)
    fun checkCommands(oldCommands: List<Map<String, *>>, newCommands: List<Map<String, *>>): Boolean
}