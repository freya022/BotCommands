package io.github.freya022.botcommands.internal.commands.autobuilder.metadata

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.internal.core.ClassPathFunction

internal abstract class CommandFunctionMetadata<A : Annotation>(
    private val classPathFunction: ClassPathFunction,
    val annotation: A,
    val path: CommandPath
) : MetadataFunctionHolder {
    final override val func get() = classPathFunction.function

    val instance: Any
        get() = classPathFunction.instance
}
