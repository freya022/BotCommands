package io.github.freya022.botcommands.api.parameters

import kotlin.reflect.KParameter
import kotlin.reflect.KType

interface MethodParameter {
    val kParameter: KParameter
    val name: String
    val type: KType
    val index: Int
    val isNullableOrOptional: Boolean
    /** This checks the java primitiveness, not kotlin, kotlin.Double is an object. */
    val isPrimitive: Boolean
}