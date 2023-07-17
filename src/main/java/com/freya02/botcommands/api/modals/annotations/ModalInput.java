package com.freya02.botcommands.api.modals.annotations

/**
 * Set this parameter as a modal input.
 *
 * The specified input name must be the same as the input name given in, for example, [Modals.createTextInput].
 *
 * @see ModalData @ModalData
 * @see ModalHandler @ModalHandler
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ModalInput(
    /**
     * The name of the modal input.<br>
     * Must match the input name provided in, for example, [Modals.createTextInput].
     */
    val name: String
)
