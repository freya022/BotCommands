package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.components.builder.IPersistentActionableComponent
import io.github.freya022.botcommands.api.components.serialization.annotations.SerializableComponentData
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.ComponentParameterResolver

/**
 * Sets this parameter as data coming from [IPersistentActionableComponent.bindTo].
 *
 * The order and types of the passed data must match with the handler parameters.
 *
 * ### Requirements
 * A compatible [ComponentParameterResolver] must exist for the annotated parameter,
 * the default supported types can be seen in [ParameterResolver].
 *
 * If your parameter is a serializable object,
 * you can instead use [@SerializableComponentData][SerializableComponentData].
 *
 * @see JDAButtonListener @JDAButtonListener
 * @see JDASelectMenuListener @JDASelectMenuListener
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ComponentData
