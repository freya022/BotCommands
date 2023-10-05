package io.github.freya022.botcommands.internal.commands.mixins

import io.github.freya022.botcommands.api.commands.CommandPath

interface INamedCommand {
    val parentInstance: INamedCommand?

    val name: String
    val path: CommandPath

    companion object {
        fun INamedCommand.computePath(): CommandPath {
            val components: MutableList<String> = arrayListOf()
            var info = this

            do {
                components += info.name
                info = info.parentInstance ?: break
            } while (true)

            return CommandPath.of(*components.also { it.reverse() }.toTypedArray())
        }
    }
}