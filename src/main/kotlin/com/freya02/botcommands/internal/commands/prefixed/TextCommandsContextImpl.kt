package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.TextCommandsContext
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.throwUser

class TextCommandsContextImpl internal constructor(context: BContextImpl) : TextCommandsContext {
    private val textCommandMap: MutableMap<String, TextCommandInfo> = hashMapOf()

    fun addTextCommand(commandInfo: TextCommandInfo) {
        (commandInfo.aliases + commandInfo.name).forEach { name ->
            textCommandMap.put(name, commandInfo)?.let {
                throwUser(commandInfo.variations.first().method, "Text command with path ${commandInfo.path} already exists")
            }
        }
    }

    fun findTextCommand(words: List<String>): TextCommandInfo? {
        val initial = textCommandMap[words.first()] ?: return null
        return words
            .drop(1) //First word is already resolved
            .fold(initial) { info, subname ->
                info.subcommands[subname] ?: return null
            }
    }

    fun findTextSubcommands(words: List<String>): Collection<TextCommandInfo> {
        val command = findTextCommand(words) ?: return emptyList()
        return command.subcommands.values
    }

    fun getRootCommands(): Collection<TextCommandInfo> {
        return textCommandMap.values
    }
}