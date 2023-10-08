package io.github.freya022.botcommands.internal.commands.prefixed

import io.github.freya022.botcommands.api.commands.prefixed.TextCommandsContext
import io.github.freya022.botcommands.internal.utils.throwUser

class TextCommandsContextImpl internal constructor() : TextCommandsContext {
    private val textCommandMap: MutableMap<String, TopLevelTextCommandInfo> = hashMapOf()

    internal fun addTextCommand(commandInfo: TopLevelTextCommandInfo) {
        (commandInfo.aliases + commandInfo.name).forEach { name ->
            textCommandMap.put(name, commandInfo)?.let {
                throwUser(commandInfo.variations.first().function, "Text command with path '${commandInfo.path}' already exists")
            }
        }
    }

    fun findTextCommand(words: List<String>): TextCommandInfo? {
        val initial: TextCommandInfo = textCommandMap[words.first()] ?: return null
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

    override fun getRootCommands(): Collection<TopLevelTextCommandInfo> {
        return textCommandMap.values
    }
}