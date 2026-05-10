package io.github.freya022.botcommands.internal.commands.utils

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.INamedCommand

fun INamedCommand.lazyPath(): Lazy<CommandPath> = lazy {
    val components = mutableListOf<String>()
    var info = this

    do {
        components.add(index = 0, info.name)
        info = info.parentInstance ?: break
    } while (true)

    CommandPath.of(components)
}
