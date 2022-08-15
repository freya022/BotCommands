package com.freya02.botcommands.internal.prefixed

import com.freya02.botcommands.api.application.CommandPath
import java.util.*

typealias FirstName = String
typealias LastName = String

class TextCommandTree {
    internal val children: MutableMap<FirstName, TextCommandTree> = hashMapOf()
    private val commands: MutableMap<LastName, TreeSet<TextCommandInfo>> = hashMapOf()

    fun getCommands(): Collection<TextCommandInfo> {
        return commands.values
    }

    fun addCommand(path: CommandPath, info: TextCommandInfo) {
        commands.computeIfAbsent(path.lastName) { TreeSet(TextCommandComparator()) }.add(info)
    }
}