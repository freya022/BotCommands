package io.github.freya022.botcommands.api.modals.annotations

import io.github.freya022.botcommands.api.parameters.resolvers.ModalParameterResolver

/**
 * Set this parameter as a modal input.
 *
 * The specified input custom ID must be the same as the custom ID of a component in that modal.
 *
 * The supported data types can be seen in [ModalParameterResolver], more types can be supported by implementing it.
 *
 * @see ModalData @ModalData
 * @see ModalHandler @ModalHandler
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ModalInput(
    /**
     * The custom ID this modal input will match against.
     */
    @get:JvmName("value") val customId: String
)
