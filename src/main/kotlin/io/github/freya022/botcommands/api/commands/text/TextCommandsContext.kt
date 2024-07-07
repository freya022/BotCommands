package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.core.DefaultEmbedFooterIconSupplier
import io.github.freya022.botcommands.api.core.DefaultEmbedSupplier
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService

/**
 * Helps to get the registered text commands.
 */
@InterfacedService(acceptMultiple = false)
interface TextCommandsContext {
    val rootCommands: Collection<TopLevelTextCommandInfo>

    /**
     * Returns the [DefaultEmbedSupplier] service.
     *
     * @see DefaultEmbedSupplier
     */
    val defaultEmbedSupplier: DefaultEmbedSupplier

    /**
     * Returns the [DefaultEmbedFooterIconSupplier] service.
     *
     * @see DefaultEmbedFooterIconSupplier
     */
    val defaultEmbedFooterIconSupplier: DefaultEmbedFooterIconSupplier

    fun findTextCommand(words: List<String>): TextCommandInfo?

    fun findTextSubcommands(words: List<String>): Collection<TextCommandInfo>
}