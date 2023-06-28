package com.freya02.botcommands.api.core.utils

import com.freya02.botcommands.internal.utils.javaMethodInternal
import com.freya02.botcommands.internal.utils.throwInternal
import java.lang.reflect.Executable
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure

object ReflectionUtils { //For Java users
    @JvmStatic
    fun <T : Any> Class<T>.toKotlin(): KClass<T> = this.kotlin

    @JvmStatic
    fun <T : Any> KClass<T>.toJava(): Class<T> = this.java
}

fun KClass<*>.isSubclassOfAny(vararg classes: KClass<*>): Boolean = classes.any { this.isSubclassOf(it) }
fun KClass<*>.isSubclassOfAny(classes: Iterable<KClass<*>>): Boolean = classes.any { this.isSubclassOf(it) }

val KParameter.isPrimitive: Boolean
    get() = this.type.jvmErasure.java.isPrimitive || this.type.jvmErasure.javaPrimitiveType != null

val KParameter.bestName: String
    get() = this.name ?: "arg${this.index}"

val KType.simpleNestedName: String
    get() = buildString {
        append(jvmErasure.simpleNestedName)
        if (arguments.isNotEmpty()) {
            append("<")
            append(arguments.joinToString {
                it.type?.simpleNestedName ?: "*"
            })
            append(">")
        }
        if (isMarkedNullable) append("?")
    }

val KClass<*>.shortQualifiedName
    get() = java.packageName.split('.').joinToString(".") { it.first().toString() } + "." + simpleNestedName

val KClass<*>.simpleNestedName: String
    inline get() = this.java.simpleNestedName

val Class<*>.simpleNestedName: String
    get() = when {
        this.isPrimitive -> canonicalName
        this.isArray -> componentType.simpleNestedName + "[]"
        else -> this.canonicalName.substring(this.packageName.length + 1)
    }

val KFunction<*>.isStatic: Boolean
    get() = !isConstructor && Modifier.isStatic(this.javaMethodInternal.modifiers)

val KFunction<*>.isConstructor: Boolean
    get() = this.javaConstructor != null

val KFunction<*>.javaMethodOrConstructorOrNull: Executable?
    get() = javaMethod ?: javaConstructor

val KFunction<*>.javaMethodOrConstructor: Executable
    get() = javaMethodOrConstructorOrNull ?: throwInternal(this, "Could not resolve Java method or constructor")