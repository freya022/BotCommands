package io.github.freya022.botcommands.api.components.serialization.annotations

import io.github.freya022.botcommands.api.components.annotations.TimeoutData
import io.github.freya022.botcommands.api.components.serialization.GlobalComponentDataSerializer

/**
 * Same as [@TimeoutData][TimeoutData],
 * but also generates a resolver which (de)serializes the value using the [GlobalComponentDataSerializer].
 */
@TimeoutData
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
annotation class SerializableTimeoutData