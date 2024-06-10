package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.TextCommandsContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.utils.throwUser

@BService
internal class TextCommandsContextImpl internal constructor() : TextCommandsContext {
    private val textCommandMap: MutableMap<String, TopLevelTextCommandInfoImpl> = hashMapOf()

    override val rootCommands: Collection<TopLevelTextCommandInfoImpl>
        get() = textCommandMap.values.toList()

    internal fun addTextCommand(commandInfo: TopLevelTextCommandInfoImpl) {
        (commandInfo.aliases + commandInfo.name).forEach { name ->
            textCommandMap.put(name, commandInfo)?.let {
                throwUser(commandInfo.variations.first().function, "Text command with path '${commandInfo.path}' already exists")
            }
        }
    }

    override fun findTextCommand(words: List<String>): TextCommandInfoImpl? {
        val initial: TextCommandInfoImpl = textCommandMap[words.first()] ?: return null
        return words
            .drop(1) //First word is already resolved
            .fold(initial) { info, subname ->
                info.subcommands[subname] ?: return null
            }
    }

    override fun findTextSubcommands(words: List<String>): Collection<TextCommandInfoImpl> {
        val command = findTextCommand(words) ?: return emptyList()
        return command.subcommands.values
    }
}