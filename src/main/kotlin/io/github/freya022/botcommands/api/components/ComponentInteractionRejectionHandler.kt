package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

/**
 * Processes component interaction rejections returned by [component interaction filters][ComponentInteractionFilter].
 *
 * ### Usage
 * - Register your instance as a service with [BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 * - Implement either [handle] (Java) or [handleSuspend] (Kotlin)
 *
 * @param T Type of the error object returned by [ComponentInteractionFilter]
 *
 * @see ComponentInteractionFilter
 */
@InterfacedService(acceptMultiple = false)
interface ComponentInteractionRejectionHandler<T : Any> {
    @JvmSynthetic
    suspend fun handleSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?, userData: T): Unit =
        handle(event, handlerName, userData)

    fun handle(event: GenericComponentInteractionCreateEvent, handlerName: String?, userData: T): Unit =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'handle' or 'handleSuspend' method")
}