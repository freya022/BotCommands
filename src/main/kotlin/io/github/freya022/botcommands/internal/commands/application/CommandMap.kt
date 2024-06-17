package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrow
import java.util.*

interface CommandMap<T : ApplicationCommandInfo> : Map<CommandPath, T>

internal class MutableCommandMap<T : ApplicationCommandInfo>(
    private val map: MutableMap<CommandPath, T> = Collections.synchronizedMap(
        mutableMapOf()
    )
) : CommandMap<T>, MutableMap<CommandPath, T> by map {
    override fun put(key: CommandPath, value: T): T? {
        //Check if commands with the same name as their entire path are present
        // For example, trying to insert /tag create while /tag already exists
        for ((commandPath, mapInfo) in this) {
            require(key.fullPath != commandPath.name) {
                """
                    Tried to add a command but a command with an equal/longer path already exists
                    First command: $commandPath, at ${mapInfo.declarationSite}
                    Second command: $key, at ${value.declarationSite}
                """.trimIndent()
            }

            require(commandPath.fullPath != key.name) {
                """
                    Tried to add a command but a top-level command already exists
                    First command: $commandPath, at ${mapInfo.declarationSite}
                    Second command: $key, at ${value.declarationSite}
                """.trimIndent()
            }
        }

        map.putIfAbsentOrThrow(key, value) {
            """
                Tried to add a command but a command with the same path ($key) already exists
                First command: ${it.declarationSite}
                Second command: at ${value.declarationSite}
            """.trimIndent()
        }

        return null // No previous value
    }
}

internal class UnmodifiableCommandMap<T : ApplicationCommandInfo>(map: CommandMap<T>) : CommandMap<T>,
    Map<CommandPath, T> by Collections.unmodifiableMap(map)

internal object EmptyCommandMap : CommandMap<ApplicationCommandInfo>,
                                  Map<CommandPath, ApplicationCommandInfo> by emptyMap()

internal fun <T : ApplicationCommandInfo> CommandMap<T>.toUnmodifiableMap(): CommandMap<T> =
    UnmodifiableCommandMap(this)