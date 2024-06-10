package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolder
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrow
import java.util.*

internal class NamedCommandMap<T> internal constructor() where T : INamedCommand, T : IDeclarationSiteHolder {
    private val mutableMap: MutableMap<String, T> = hashMapOf()
    internal val map: Map<String, T> = Collections.unmodifiableMap(mutableMap)
    internal val values: Collection<T> = Collections.unmodifiableCollection(mutableMap.values)

    internal fun putNewCommand(newCommand: T) {
        mutableMap.putIfAbsentOrThrow(newCommand.name, newCommand) { oldCommand ->
            """
            Command '${newCommand.path.fullPath}' is already defined
            Existing command declared at: ${oldCommand.declarationSite?.string ?: "<Declaration site unavailable>"}
            Current command declared at: ${newCommand.declarationSite?.string ?: "<Declaration site unavailable>"}
            """.trimIndent()
        }
    }

    internal fun isEmpty(): Boolean = mutableMap.isEmpty()
}