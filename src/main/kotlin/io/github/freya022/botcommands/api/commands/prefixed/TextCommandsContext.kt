package io.github.freya022.botcommands.api.commands.prefixed

import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.internal.commands.prefixed.TextCommandInfo
import io.github.freya022.botcommands.internal.commands.prefixed.TopLevelTextCommandInfo

/**
 * Helps to get the registered text commands.
 */
@InjectedService
interface TextCommandsContext {
    val rootCommands: Collection<TopLevelTextCommandInfo>

    fun findTextCommand(words: List<String>): TextCommandInfo?

    fun findTextSubcommands(words: List<String>): Collection<TextCommandInfo>
}