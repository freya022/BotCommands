package io.github.freya022.botcommands.api.core.service

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder

/**
 * Supplies an instance of the registered type.
 *
 * @param T Type of the class returned
 *
 * @see BServiceConfigBuilder.registerInstanceSupplier
 */
fun interface InstanceSupplier<T : Any> {
    fun supply(context: BContext): T
}
