package io.github.freya022.botcommands.internal.commands.application.autobuilder.utils

import io.github.freya022.botcommands.api.core.reflect.ParameterType
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.hasAnnotationRecursive
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.isNullable
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.toDiscordString
import kotlin.reflect.KParameter

/**
 * Helps to get values from the right place,
 * reduces surface of error when processing inline classes.
 */
internal class ParameterAdapter internal constructor(
    internal val originalParameter: KParameter,
    internal val valueParameter: KParameter
) {
    internal val declaredName get() = valueParameter.findDeclarationName()
    internal val discordName get() = originalParameter.findDeclarationName().toDiscordString()
    internal val actualType get() = ParameterType.ofType(valueParameter.type)
    internal val isOptionalOrNullable get() = originalParameter.isNullable || originalParameter.isOptional
            || valueParameter.isNullable || valueParameter.isOptional

    internal inline fun <reified A : Annotation> hasAnnotation() =
        originalParameter.hasAnnotationRecursive<A>()

    internal inline fun <reified A : Annotation> findAnnotation() =
        originalParameter.findAnnotationRecursive<A>()
}