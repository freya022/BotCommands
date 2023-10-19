package io.github.freya022.botcommands.api.core.utils

import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.lineNumber
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.sourceFile
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import io.github.freya022.botcommands.internal.utils.javaMethodInternal
import io.github.freya022.botcommands.internal.utils.throwInternal
import java.lang.reflect.Executable
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

/**
 * Utility class to convert between Kotlin and Java reflection objects.
 */
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

val KType.qualifiedNestedName: String
    get() = buildString {
        append(jvmErasure.jvmName)
        if (arguments.isNotEmpty()) {
            append("<")
            append(arguments.joinToString {
                it.type?.qualifiedNestedName ?: "*"
            })
            append(">")
        }
        if (isMarkedNullable) append("?")
    }

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

val Class<*>.shortQualifiedName
    get() = packageName.split('.').joinToString(".") { it.first().toString() } + "." + simpleNestedName

val KClass<*>.shortQualifiedName
    get() = this.java.shortQualifiedName

val KClass<*>.simpleNestedName: String
    inline get() = this.java.simpleNestedName

val Class<*>.simpleNestedName: String
    get() = when {
        this.isPrimitive -> canonicalName
        this.isArray -> componentType.simpleNestedName + "[]"
        this.canonicalName == null -> this.typeName.substring(this.packageName.length + 1)
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

val Class<*>.allSuperclasses: List<Class<*>>
    get() = buildList {
        var clazz: Class<*>? = superclass
        while (clazz != null) {
            add(clazz)
            clazz = clazz.superclass
        }
    }

fun KFunction<*>.getSignature(
    parameterNames: List<String> = listOf(),
    qualifiedClass: Boolean = false,
    qualifiedTypes: Boolean = false,
    returnType: Boolean = false,
    source: Boolean = true
): String = buildString {
    val declaringClassName = if (qualifiedClass) declaringClass.jvmName else declaringClass.simpleNestedName
    val methodName = name
    val parameters = valueParameters.joinToString {
        val type = if (qualifiedTypes) it.type.qualifiedNestedName else it.type.simpleNestedName
        when (it.name) {
            in parameterNames -> "${it.bestName}: $type"
            else -> type
        }
    }

    append("$declaringClassName.$methodName($parameters)")
    if (returnType) {
        append(": ")
        append(if (qualifiedTypes) this@getSignature.returnType.qualifiedNestedName else this@getSignature.returnType.simpleNestedName)
    }
    if (source) {
        val sourceStr = javaMethodOrConstructorOrNull.let { method ->
            return@let when {
                method != null && lineNumber != 0 -> {
                    val sourceFile = method.declaringClass.sourceFile
                    val lineNumber = lineNumber

                    "$sourceFile:$lineNumber"
                }
                else -> "<no-source>"
            }
        }
        append(" ($sourceStr)")
    }
}