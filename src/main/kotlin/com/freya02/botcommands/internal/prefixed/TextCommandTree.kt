package com.freya02.botcommands.internal.prefixed

import java.util.*

private typealias FirstName = String

class TextCommandTree {
    internal val children: MutableMap<FirstName, TextCommandTree> = hashMapOf()
    private val commands: TreeSet<TextCommandInfo> = TreeSet(TextCommandComparator)

    fun getCommands(): Set<TextCommandInfo> {
        return commands
    }

    fun exists(info: TextCommandInfo) = commands.any { it.method == info.method }

    fun addCommand(info: TextCommandInfo): Boolean {
        return commands.add(info)
    }
}