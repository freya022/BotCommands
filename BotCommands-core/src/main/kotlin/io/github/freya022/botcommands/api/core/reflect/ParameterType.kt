package io.github.freya022.botcommands.api.core.reflect

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

class ParameterType private constructor(val type: KType) {
    fun javaType() = type.javaType

    fun javaClass() = type.jvmErasure.java

    fun kotlinErasure() = type.jvmErasure

    fun ignoreNullability() = ParameterType(type.withNullability(false))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParameterType

        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }

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
}