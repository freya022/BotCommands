package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

/**
 * Base interface for component interaction filters.
 *
 * @see GlobalComponentInteractionFilter
 * @see ScopedComponentInteractionFilter
 */
@InterfacedService(acceptMultiple = true)
sealed interface ComponentInteractionFilter : Filter {
    /**
     * Checks if this component can be used, returns `null` if this filter passes,
     * or a reason for the rejection, used for logging purposes.
     *
     * @param handlerName The persistent handler name, as declared in [JDAButtonListener]/[JDASelectMenuListener],
     *                    might be null if there is no handler defined, or is ephemeral.
     */
    @JvmSynthetic
    suspend fun checkSuspend(event: GenericComponentInteractionCreateEvent, handlerName: String?): String? =
        check(event, handlerName)

    /**
     * Checks if this component can be used, returns `null` if this filter passes,
     * or a reason for the rejection, used for logging purposes.
     *
     * @param handlerName The persistent handler name, as declared in [JDAButtonListener]/[JDASelectMenuListener],
     *                    might be null if there is no handler defined, or is ephemeral.
     */
    fun check(event: GenericComponentInteractionCreateEvent, handlerName: String?): String? =
        throw NotImplementedError("${this.javaClass.simpleNestedName} must implement the 'isAccepted' or 'isAcceptedSuspend' method")
}
