package io.github.freya022.botcommands.api.modals.annotations

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.core.annotations.Handler
import io.github.freya022.botcommands.api.core.config.BConfigBuilder
import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.api.modals.ModalBuilder
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ModalParameterResolver
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent

/**
 * Declares this function as a modal handler for the specified modal name.
 *
 * The function must:
 *  - Be in the [search path][BConfigBuilder.addSearchPath]
 *  - Be non-static and public
 *  - Have [ModalInteractionEvent] as its first parameter
 *  - Optionally: Have all your consecutive [ModalData], specified in [ModalBuilder.bindTo]
 *  - And finally: Have all your [ModalInput] and custom parameters, in the order you want
 *
 * **Requirement:** The declaring class must be annotated with [@Handler][Handler] or [@Command][Command].
 *
 * Supported parameters are in [ParameterResolver],
 * additional types can be added by implementing [ModalParameterResolver].
 *
 * @see ModalData @ModalData
 * @see ModalInput @ModalInput
 * @see Aggregate @Aggregate
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ModalHandler(
    /**
     * The name of the handler,
     * which must be the same handler name as in [Modals.create]
     */
    @get:JvmName("value") val name: String
)
