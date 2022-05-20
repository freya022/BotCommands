package com.freya02.botcommands.api.parameters

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

class ParameterType private constructor(val type: KType) {
    companion object {
        @JvmStatic
        @JvmOverloads
        fun ofClass(clazz: Class<*>, arguments: List<Class<*>> = listOf()) =
            ParameterType(
                clazz.kotlin.createType(
                    arguments.map { KTypeProjection.invariant(it.kotlin.starProjectedType) },
                    false,
                    listOf()
                )
            )

        @JvmStatic
        fun ofKClass(clazz: KClass<*>) = ParameterType(clazz.starProjectedType)

        @JvmStatic
        fun ofType(type: KType) = ParameterType(type)
    }

    fun javaType() = type.javaType

    fun javaClass() = type.jvmErasure.java
}