package io.github.freya022.botcommands.api.components.annotations

import io.github.freya022.botcommands.api.components.builder.group.ComponentGroupBuilder
import io.github.freya022.botcommands.api.components.builder.timeout
import io.github.freya022.botcommands.api.components.data.GroupTimeoutData
import io.github.freya022.botcommands.api.core.config.BServiceConfigBuilder
import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.TimeoutParameterResolver
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import kotlin.reflect.KFunction

/**
 * Declares this function as a component group timeout handler with the given name.
 *
 * ### Requirements
 * - The declaring class must be annotated with [@BService][BService]
 * or [any annotation that enables your class for dependency injection][BServiceConfigBuilder.serviceAnnotations].
 * - The annotation value to have same name as the one given to [ComponentGroupBuilder.timeout], however,
 * it can be omitted if you use the type-safe [timeout] extensions.
 * - First parameter must be [GroupTimeoutData].
 *
 * ### Option types
 * - User data: Uses [@TimeoutData][TimeoutData], the order must match the data passed when creating the select menu,
 * supported types and modifiers are in [ParameterResolver],
 * additional types can be added by implementing [TimeoutParameterResolver].
 * - Service options: No annotation, however, I recommend injecting the service in the class instead.
 *
 * @see ComponentGroupBuilder.timeout
 *
 * @see Aggregate @Aggregate
 */
@Target(AnnotationTarget.FUNCTION)
annotation class GroupTimeoutHandler(
    /**
     * Name of the timeout handler, referenced by [ComponentGroupBuilder.timeout].
     *
     * This can be omitted if you use the type-safe [timeout] extensions.
     *
     * Defaults to `FullyQualifiedClassName.methodName`.
     */
    @get:JvmName("value") val name: String = ""
)

internal fun GroupTimeoutHandler.getEffectiveName(func: KFunction<*>): String {
    return name.ifBlank { "${func.declaringClass.qualifiedName}.${func.name}" }
}