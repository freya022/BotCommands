package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver

/**
 * Sets this parameter as data coming from [IPersistentActionableComponent.bindTo].
 *
 * The order and types of the passed data must match with the handler parameters.
 *
 * The supported data types can be seen in [ComponentParameterResolver], more types can be supported by implementing it.
 *
 * @see JDAButtonListener @JDAButtonListener
 * @see JDASelectMenuListener @JDASelectMenuListener
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ComponentData
