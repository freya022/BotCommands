package com.freya02.botcommands.api.components

import com.freya02.botcommands.api.core.CooldownService
import com.freya02.botcommands.api.core.config.BServiceConfigBuilder
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.InterfacedService
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

/**
 * Filters component interactions (such as buttons and select menus),
 * any filter that returns `false` prevents the interaction from executing.
 *
 * Filters are tested right before the component gets executed (i.e., after the component constraints were checked).
 *
 * **Note:** Your filter still has to acknowledge the interaction in case it rejects it.
 *
 * **Usage**: Register your instance as a service with [BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 *
 * @see InterfacedService @InterfacedService
 *
 * @see isAccepted
 * @see CooldownService
 */
@InterfacedService(acceptMultiple = true)
interface ComponentInteractionFilter {
    /**
     * Returns whether the component interaction should be accepted or not.
     *
     * **Note:** Your filter still has to acknowledge the interaction in case it rejects it.
     *
     * @return `true` if the component interaction can run, `false` otherwise
     *
     * @see ComponentInteractionFilter
     */
    @JvmSynthetic
    suspend fun isAcceptedSuspend(event: GenericComponentInteractionCreateEvent): Boolean =
        isAccepted(event)

    /**
     * Returns whether the component interaction should be accepted or not.
     *
     * **Note:** Your filter still has to acknowledge the interaction in case it rejects it.
     *
     * @return `true` if the component interaction can run, `false` otherwise
     *
     * @see ComponentInteractionFilter
     */
    fun isAccepted(event: GenericComponentInteractionCreateEvent): Boolean =
        throw UnsupportedOperationException("${this.javaClass.simpleName} must implement the 'isAccepted' or 'isAcceptedSuspend' method")
}
