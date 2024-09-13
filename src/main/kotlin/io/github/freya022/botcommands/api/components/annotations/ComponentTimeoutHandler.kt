package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent
import io.github.freya022.botcommands.api.components.builder.timeoutWith
import io.github.freya022.botcommands.api.components.data.ComponentTimeoutData
import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import kotlin.reflect.KFunction

/**
 * Declares this function as a component timeout handler with the given name.
 *
 * ### Requirements
 * - The declaring class must be annotated with [@BService][BService].
 * - The annotation value to have same name as the one given to [IPersistentTimeoutableComponent.timeout], however,
 * it can be omitted if you use the type-safe [timeoutWith] extensions.
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
annotation class ComponentTimeoutHandler(
    /**
     * Name of the timeout handler, referenced by [IPersistentTimeoutableComponent.timeout].
     *
     * This can be omitted if you use the type-safe [timeoutWith] extensions.
     *
     * Defaults to `FullyQualifiedClassName.methodName`.
     */
    @get:JvmName("value") val name: String = ""
)

internal fun ComponentTimeoutHandler.getEffectiveName(func: KFunction<*>): String {
    return name.ifBlank { "${func.declaringClass.qualifiedName}.${func.name}" }
}
