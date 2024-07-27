package io.github.freya022.botcommands.internal.commands.application.diff

import io.github.freya022.botcommands.internal.application.diff.DiffLogger

internal interface ApplicationCommandDiffEngine {
    context(DiffLogger)
    fun checkCommands(oldCommands: List<Map<String, *>>, newCommands: List<Map<String, *>>): Boolean
}