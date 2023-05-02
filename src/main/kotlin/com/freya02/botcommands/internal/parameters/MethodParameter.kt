package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.internal.findDeclarationName
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure

interface MethodParameter {
    val kParameter: KParameter
    val name: String
        get() = kParameter.findDeclarationName()

    val type: KType
        get() = kParameter.type
    val index: Int
        get() = kParameter.index
    val isOptional: Boolean
    /** This checks the java primitiveness, not kotlin, kotlin.Double is an object. */
    val isPrimitive: Boolean
        get() = kParameter.type.jvmErasure.java.isPrimitive
}