package com.freya02.botcommands.internal.application

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.Utils
import java.util.*

interface CommandMap<T : ApplicationCommandInfo> : Map<CommandPath, T>

class MutableCommandMap<T : ApplicationCommandInfo>(
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
                        key, Utils.formatMethodShort(value.method),
                        commandPath, Utils.formatMethodShort(mapInfo.method)
                    )
                )
            }

            if (commandPath.fullPath == key.name) {
                throwUser(
                    "Tried to add a command with path '%s' (at %s) but a top level command already exists: '%s' (at %s)".format(
                        key, Utils.formatMethodShort(value.method),
                        commandPath, Utils.formatMethodShort(mapInfo.method)
                    )
                )
            }
        }

        val oldInfo = map.put(key, value)
        if (oldInfo != null) {
            throwUser(
                "Tried to add a command with path '%s' (at %s) but an equal path already exists: '%s' (at %s)".format(
                    key,
                    Utils.formatMethodShort(value.method),
                    oldInfo.path,
                    Utils.formatMethodShort(oldInfo.method)
                )
            )
        }

        return null //oldInfo is always null
    }
}