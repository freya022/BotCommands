package io.github.freya022.botcommands.api.core

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import java.io.InputStream

/**
 * Interface for embed footer icons requested by [BaseCommandEvent.getDefaultIconStream].
 *
 * Returns `null` by default.
 *
 * **Usage**: Register your instance as a service with [BService].
 *
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = false)
fun interface DefaultEmbedFooterIconSupplier {

    fun get(): InputStream?
}
