package io.github.freya022.botcommands.internal.commands.mixins

import io.github.freya022.botcommands.api.commands.CommandPath
import java.util.*

interface INamedCommand {
    val parentInstance: INamedCommand?

    val name: String
    val path: CommandPath

    companion object {
        fun INamedCommand.computePath(): CommandPath {
            val components = LinkedList<String>()
            var info = this

            do {
                components.addFirst(info.name)
                info = info.parentInstance ?: break
            } while (true)

            return CommandPath.of(components)
        }
    }
}