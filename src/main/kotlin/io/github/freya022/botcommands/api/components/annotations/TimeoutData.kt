package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent
import io.github.freya022.botcommands.api.components.serialization.annotations.SerializableTimeoutData
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver

/**
 * Sets this parameter as data coming from [IPersistentTimeoutableComponent.timeout].
 *
 * The order and types of the passed data must match with the handler parameters.
 *
 * ### Requirements
 * A compatible [TimeoutParameterResolver] must exist for the annotated parameter,
 * the default supported types can be seen in [ParameterResolver].
 *
 * If your parameter is a serializable object,
 * you can instead use [@SerializableTimeoutData][SerializableTimeoutData].
 *
 * @see ComponentTimeoutHandler @ComponentTimeoutHandler
 * @see GroupTimeoutHandler @GroupTimeoutHandler
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TimeoutData
