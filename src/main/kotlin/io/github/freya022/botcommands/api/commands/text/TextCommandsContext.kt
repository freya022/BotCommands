package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.internal.commands.text.TextCommandInfo
import io.github.freya022.botcommands.internal.commands.text.TopLevelTextCommandInfo

/**
 * Helps to get the registered text commands.
 */
@InjectedService
interface TextCommandsContext {
    val rootCommands: Collection<TopLevelTextCommandInfo>

    fun findTextCommand(words: List<String>): TextCommandInfo?

    fun findTextSubcommands(words: List<String>): Collection<TextCommandInfo>
}