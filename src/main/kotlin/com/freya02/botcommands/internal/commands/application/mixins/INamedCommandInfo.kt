package com.freya02.botcommands.internal.commands.application.mixins

import com.freya02.botcommands.api.commands.CommandPath

interface INamedCommandInfo {
    val parentInstance: INamedCommandInfo?

    val name: String
    val _path: CommandPath

    companion object {
        fun INamedCommandInfo.computePath(): CommandPath {
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