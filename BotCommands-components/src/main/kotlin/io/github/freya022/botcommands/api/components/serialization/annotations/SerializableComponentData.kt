package io.github.freya022.botcommands.api.components.serialization.annotations

import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.components.serialization.GlobalComponentDataSerializer

/**
 * Same as [@ComponentData][ComponentData],
 * but also generates a resolver which (de)serializes the value using the [GlobalComponentDataSerializer].
 */
@ComponentData
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
annotation class SerializableComponentData