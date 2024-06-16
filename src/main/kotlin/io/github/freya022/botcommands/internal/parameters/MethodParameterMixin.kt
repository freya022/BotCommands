package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.parameters.MethodParameter
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.isNullable
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

internal interface MethodParameterMixin : MethodParameter {
    /**
     * This is needed because autocomplete borrows the parameters and options of its slash command
     * But autocomplete execution needs the parameters of the autocomplete handler
     */
    val executableParameter: KParameter
        get() = kParameter
}

internal abstract class AbstractMethodParameter internal constructor(final override val kParameter: KParameter) : MethodParameterMixin {
    final override val name = kParameter.findDeclarationName()
    final override val isNullableOrOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }
    final override val type = kParameter.type
    final override val index = kParameter.index
    final override val isPrimitive = kParameter.type.jvmErasure.java.isPrimitive
}