package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent
import io.github.freya022.botcommands.api.components.data.ComponentTimeoutData
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver

/**
 * Declares this function as a component timeout handler with the given name.
 *
 * ### Requirements
 * - The declaring class must be annotated with [@BService][BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 * - The annotation value to have same name as the one given to [IPersistentTimeoutableComponent.timeout].
 * - First parameter must be [ComponentTimeoutData].
 *
 * ### Option types
 * - User data: Uses [@TimeoutData][TimeoutData], the order must match the data passed when creating the select menu,
 * supported types and modifiers are in [ParameterResolver],
 * additional types can be added by implementing [TimeoutParameterResolver].
 * - Service options: No annotation, however, I recommend injecting the service in the class instead.
 *
 * @see IPersistentTimeoutableComponent.timeout
 *
 * @see Aggregate @Aggregate
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ComponentTimeoutHandler(@get:JvmName("value") val name: String)
