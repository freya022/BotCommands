package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable
import com.freya02.botcommands.internal.utils.findDeclarationName
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

interface MethodParameter {
    val kParameter: KParameter
    /**
     * This is needed because autocomplete borrows the parameters and options of its slash command
     * But autocomplete execution needs the parameters of the autocomplete handler
     */
    val executableParameter: KParameter
        get() = kParameter
    val name: String
    val type: KType
    val index: Int
    val isNullableOrOptional: Boolean
    /** This checks the java primitiveness, not kotlin, kotlin.Double is an object. */
    val isPrimitive: Boolean
}

open class MethodParameterImpl(final override val kParameter: KParameter) : MethodParameter {
    final override val name = kParameter.findDeclarationName()
    final override val isNullableOrOptional: Boolean by lazy { kParameter.isNullable || kParameter.isOptional }
    final override val type = kParameter.type
    final override val index = kParameter.index
    final override val isPrimitive = kParameter.type.jvmErasure.java.isPrimitive
}