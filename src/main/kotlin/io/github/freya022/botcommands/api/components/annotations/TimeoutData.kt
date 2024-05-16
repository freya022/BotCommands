package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent

/**
 * Sets this parameter as data coming from [IPersistentTimeoutableComponent.timeout].
 *
 * The order and types of the passed data must match with the handler parameters.
 *
 * @see ComponentTimeoutHandler @ComponentTimeoutHandler
 * @see GroupTimeoutHandler @GroupTimeoutHandler
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class TimeoutData
