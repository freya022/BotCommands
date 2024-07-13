package io.github.freya022.botcommands.api.core.utils

import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.lineNumberOrNull
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.sourceFileOrNull
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import io.github.freya022.botcommands.internal.utils.javaMethodInternal
import io.github.freya022.botcommands.internal.utils.throwInternal
import java.lang.reflect.Executable
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
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

inline fun <reified T : Any> KClass<*>.isSuperclassOf(): Boolean = this.isAssignableFrom(T::class)
inline fun <reified T : Any> KClass<*>.isSubclassOf(): Boolean = T::class.isAssignableFrom(this)

inline fun <reified T : Any> Class<*>.isSubclassOf(): Boolean = T::class.java.isAssignableFrom(this)

fun KClass<*>.isSubclassOf(kClass: KClass<*>): Boolean = kClass.isAssignableFrom(this)

fun KClass<*>.isAssignableFrom(clazz: Class<*>) = this.java.isAssignableFrom(clazz)
fun KClass<*>.isAssignableFrom(kClass: KClass<*>) = this.java.isAssignableFrom(kClass.java)

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

val Class<*>.allSuperclassesAndInterfaces: List<Class<*>>
    get() = buildList {
        val visited = hashSetOf<Class<*>>()
        val queue = ArrayDeque<Class<*>>()

        visited += this@allSuperclassesAndInterfaces
        queue += this@allSuperclassesAndInterfaces

        while (queue.isNotEmpty()) {
            val c = queue.removeFirst()
            this += c // Add to list

            // Get next elements
            (c.interfaces + c.superclass).filterNotNull().forEach {
                if (visited.add(it)) {
                    queue += it
                }
            }
        }
    }

fun KFunction<*>.getSignature(
    parameterNames: List<String> = listOf(),
    qualifiedClass: Boolean = false,
    qualifiedTypes: Boolean = false,
    returnType: Boolean = false,
    source: Boolean = true
): String = buildString {
    val declaringClassName = when {
        qualifiedClass -> declaringClass.let { it.qualifiedName ?: it.jvmName }
        else -> declaringClass.simpleNestedName
    }
    val methodName = name
    val parameters = getParameters(parameterNames, qualifiedTypes)

    append("$declaringClassName.$methodName($parameters)")
    if (returnType)
        append(": ${getReturnType(qualifiedTypes)}")
    if (source)
        append(" (${getSource()})")
}

fun KFunction<*>.getParameters(parameterNames: List<String>, qualifiedTypes: Boolean): String {
    return valueParameters.joinToString {
        val type = if (qualifiedTypes) it.type.qualifiedNestedName else it.type.simpleNestedName
        when (it.name) {
            in parameterNames -> "${it.bestName}: $type"
            else -> type
        }
    }
}

private fun KFunction<*>.getReturnType(qualifiedTypes: Boolean): String {
    return if (qualifiedTypes) {
        returnType.qualifiedNestedName
    } else {
        returnType.simpleNestedName
    }
}

private fun KFunction<*>.getSource(): String {
    val executable = javaMethodOrConstructorOrNull ?: return "<no-source>"

    val sourceFile = executable.declaringClass.sourceFileOrNull ?: return "<no-source>"
    val lineNumber = lineNumberOrNull ?: 0

    return "$sourceFile:$lineNumber"
}

fun KClass<*>.toBoxed(): KClass<*> {
    return this.java.toBoxed().kotlin
}

fun Class<*>.toBoxed(): Class<*> {
    if (!isPrimitive) return this
    if (this == Integer.TYPE) return Int::class.javaObjectType
    if (this == java.lang.Long.TYPE) return Long::class.javaObjectType
    if (this == java.lang.Boolean.TYPE) return Boolean::class.javaObjectType
    if (this == java.lang.Byte.TYPE) return Byte::class.javaObjectType
    if (this == Character.TYPE) return Char::class.javaObjectType
    if (this == java.lang.Float.TYPE) return Float::class.javaObjectType
    if (this == java.lang.Double.TYPE) return Double::class.javaObjectType
    if (this == java.lang.Short.TYPE) return Short::class.javaObjectType
    return this
}