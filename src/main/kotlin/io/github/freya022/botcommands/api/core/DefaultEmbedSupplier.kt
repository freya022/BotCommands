package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import net.dv8tion.jda.api.EmbedBuilder

/**
 * Interface for embeds requested by [BaseCommandEvent.getDefaultEmbed], aiming to reduce boilerplate.
 * This embed is also used in the default help command.
 *
 * Returns an empty [EmbedBuilder] by default.
 *
 * **Usage**: Register your instance as a service with [BService].
 *
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = false)
fun interface DefaultEmbedSupplier {

    fun get(): EmbedBuilder
}
