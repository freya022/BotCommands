package com.freya02.botcommands.api.modals.annotations

import com.freya02.botcommands.api.modals.Modals

/**
 * Set this parameter as a modal input.
 *
 * The specified input name must be the same as the input name given in, for example, [Modals.createTextInput].
 *
 * @see ModalData @ModalData
 * @see ModalHandler @ModalHandler
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ModalInput(
    /**
     * The name of the modal input.<br>
     * Must match the input name provided in, for example, [Modals.createTextInput].
     */
    val name: String
)
