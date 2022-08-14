package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

interface MethodParameter {
    val methodParameterType: MethodParameterType
    val kParameter: KParameter
    val name: String
        get() = kParameter.findDeclarationName()
    val discordName: String
        get() = throwInternal("MethodParameter#discordName is not implemented")

    val type: KType
        get() = kParameter.type
    val index: Int
        get() = kParameter.index
    val isOption: Boolean
        get() = methodParameterType == MethodParameterType.OPTION
    val isOptional: Boolean
        get() = kParameter.isNullable
    /** This checks the java primitiveness, not kotlin, kotlin.Double is an object. */
    val isPrimitive: Boolean
        get() = kParameter.type.jvmErasure.java.isPrimitive
}