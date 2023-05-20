package com.freya02.botcommands.api.core.options.annotations

import com.freya02.botcommands.api.components.annotations.JDAButtonListener
import com.freya02.botcommands.api.modals.annotations.ModalHandler

/**
 * Annotation used to mark a parameter as being the result of an aggregate.
 *
 * An aggregate is an object containing multiple options, and can have nested aggregates (when declared using a DSL).
 *
 * If this is used on an annotated method, the framework will take the constructor of that parameter as its options,
 * so, annotations must be on the constructor's parameters instead.
 *
 * **Note:** The first parameter can be the event or a subtype of it, but is optional.
 *
 * Can be used on parameters of [ModalHandler] or [JDAButtonListener] functions.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Aggregate
