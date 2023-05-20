package com.freya02.botcommands.api.core.options.annotations

import com.freya02.botcommands.api.components.annotations.JDAButtonListener
import com.freya02.botcommands.api.modals.annotations.ModalHandler

/**
 * Annotation used to mark a parameter as being the result of an aggregate.
 *
 * The framework will take the constructor of that parameter as its options, so, annotations must be on the constructor's parameters instead.
 *
 * Can be used on parameters of [ModalHandler] or [JDAButtonListener] functions.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Aggregate
