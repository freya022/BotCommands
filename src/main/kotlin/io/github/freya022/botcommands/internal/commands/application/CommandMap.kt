package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.internal.utils.shortSignatureNoSrc
import io.github.freya022.botcommands.internal.utils.throwUser
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
            if (key.fullPath == commandPath.name) {
                throwUser(
                    "Tried to add a command with path '%s' (at %s) but a equal/longer path already exists: '%s' (at %s)".format(
                        key, value.function.shortSignatureNoSrc,
                        commandPath, mapInfo.function.shortSignatureNoSrc
                    )
                )
            }

            if (commandPath.fullPath == key.name) {
                throwUser(
                    "Tried to add a command with path '%s' (at %s) but a top level command already exists: '%s' (at %s)".format(
                        key, value.function.shortSignatureNoSrc,
                        commandPath, mapInfo.function.shortSignatureNoSrc
                    )
                )
            }
        }

        val oldInfo = map.put(key, value)
        if (oldInfo != null) {
            throwUser(
                "Tried to add a command with path '%s' (at %s) but an equal path already exists: '%s' (at %s)".format(
                    key,
                    value.function.shortSignatureNoSrc,
                    oldInfo.path,
                    oldInfo.function.shortSignatureNoSrc
                )
            )
        }

        return null //oldInfo is always null
    }
}

internal class UnmodifiableCommandMap<T : ApplicationCommandInfo>(map: CommandMap<T>) : CommandMap<T>,
    Map<CommandPath, T> by Collections.unmodifiableMap(map)

internal object EmptyCommandMap : CommandMap<ApplicationCommandInfo>,
                                  Map<CommandPath, ApplicationCommandInfo> by emptyMap()

internal fun <T : ApplicationCommandInfo> CommandMap<T>.toUnmodifiableMap(): CommandMap<T> =
    UnmodifiableCommandMap(this)