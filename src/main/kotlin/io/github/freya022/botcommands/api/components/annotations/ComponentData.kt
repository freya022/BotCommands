package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent

/**
 * Sets this parameter as data coming from [IPersistentActionableComponent.bindTo].
 *
 * The order and types of the passed data must match with the handler parameters.
 *
 * @see JDAButtonListener @JDAButtonListener
 * @see JDASelectMenuListener @JDASelectMenuListener
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class ComponentData
