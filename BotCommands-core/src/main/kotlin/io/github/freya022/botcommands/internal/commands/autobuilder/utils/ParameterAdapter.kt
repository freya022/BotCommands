package io.github.freya022.botcommands.internal.commands.autobuilder.utils

import io.github.freya022.botcommands.api.core.reflect.ParameterType
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.hasAnnotationRecursive
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.isNullable
import io.github.freya022.botcommands.internal.utils.toDiscordString
import kotlin.reflect.KParameter

/**
 * Helps to get values from the right place,
 * reduces surface of error when processing inline classes.
 */
class ParameterAdapter(
    val originalParameter: KParameter,
    val valueParameter: KParameter
) {
    val declaredName get() = valueParameter.findDeclarationName()
    val discordName get() = originalParameter.findDeclarationName().toDiscordString()
    val actualType get() = ParameterType.ofType(valueParameter.type)
    internal val isOptionalOrNullable get() = originalParameter.isNullable || originalParameter.isOptional
            || valueParameter.isNullable || valueParameter.isOptional

    inline fun <reified A : Annotation> hasAnnotation() =
        originalParameter.hasAnnotationRecursive<A>()

    inline fun <reified A : Annotation> findAnnotation() =
        originalParameter.findAnnotationRecursive<A>()
}
