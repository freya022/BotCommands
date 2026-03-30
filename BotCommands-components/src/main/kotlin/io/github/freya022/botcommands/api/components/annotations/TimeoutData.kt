package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver

/**
 * Sets this parameter as data coming from [IPersistentTimeoutableComponent.timeout].
 *
 * The order and types of the passed data must match with the handler parameters.
 *
 * The supported data types can be seen in [TimeoutParameterResolver], more types can be supported by implementing it.
 *
 * @see ComponentTimeoutHandler @ComponentTimeoutHandler
 * @see GroupTimeoutHandler @GroupTimeoutHandler
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TimeoutData
