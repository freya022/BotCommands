package io.github.freya022.botcommands.api.core.options.annotations

import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler

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
 * Can be used on parameters of [@ModalHandler][ModalHandler], [@JDASelectMenuListener][JDASelectMenuListener]
 * or [@JDAButtonListener][JDAButtonListener] functions.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Aggregate
