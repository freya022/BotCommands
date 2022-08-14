package com.freya02.botcommands.internal.prefixed

import com.freya02.botcommands.api.application.CommandPath

typealias FirstName = String
typealias LastName = String

class TextCommandTree {
    internal val children: MutableMap<FirstName, TextCommandTree> = hashMapOf()
    private val commands: MutableMap<LastName, TextCommandInfo> = hashMapOf()

    fun getCommands(): Collection<TextCommandInfo> {
        return commands.values
    }

    fun exists(path: CommandPath): Boolean {
        return commands[path.lastName] != null
    }

    fun addCommand(path: CommandPath, info: TextCommandInfo) {
        commands[path.lastName] = info
    }
}