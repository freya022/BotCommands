package io.github.freya022.botcommands.api.modals.annotations

import io.github.freya022.botcommands.api.modals.ModalBuilder

/**
 * Sets this parameter as data coming from [ModalBuilder.bindTo].
 *
 * The data supplied in the method above must be in the same order as the modal handler parameters,
 * and must match the types.
 *
 * @see ModalHandler @ModalHandler
 * @see ModalInput @ModalInput
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ModalData
