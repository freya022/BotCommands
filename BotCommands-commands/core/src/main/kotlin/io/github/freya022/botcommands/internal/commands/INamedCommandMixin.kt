package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.INamedCommand

/**
 * A command with a name and possibly a parent.
 */
interface INamedCommandMixin : INamedCommand {
    fun lazyPath(): Lazy<CommandPath> = lazy {
        val components: MutableList<String> = arrayListOf()
        var info: INamedCommand = this

        do {
            components.add(index = 0, info.name)
            info = info.parentInstance ?: break
        } while (true)

        CommandPath.of(components)
    }
}
