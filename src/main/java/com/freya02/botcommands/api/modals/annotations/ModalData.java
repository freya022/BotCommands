package com.freya02.botcommands.api.modals.annotations

/**
 * Sets this parameter as data coming from [ModalBuilder.bindTo].
 *
 * The data supplied in the method above must be in the same order as the modal handler parameters,
 * and must match the types.
 *
 * @see ModalHandler @ModalHandler
 * @see ModalInput @ModalInput
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ModalData
