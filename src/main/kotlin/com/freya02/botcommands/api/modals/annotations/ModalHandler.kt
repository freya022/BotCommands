package com.freya02.botcommands.api.modals.annotations

import com.freya02.botcommands.api.core.options.annotations.Aggregate;

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
 * @see ModalData @ModalData
 * @see ModalInput @ModalInput
 * @see Aggregate @Aggregate
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ModalHandler(
    /**
     * The name of the handler,
     * which must be the same handler name as in [Modals.create]
     */
    val name: String
)
