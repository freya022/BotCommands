package dev.freya02.botcommands.jda.ktx.utils

import dev.freya02.botcommands.jda.ktx.IgnoreForMatch
import io.github.classgraph.*

fun MethodInfo.getJavaInvocationParameterTypes(): List<String> {
    return listOf(this.className) + this.parameterInfo.map { it.fullTypeName }
}

fun MethodInfo.getKotlinInvocationParameterTypes(): List<String> {
    return this.parameterInfo.filterNot { it.hasAnnotation(IgnoreForMatch::class.java) }.map { it.fullTypeName } - "kotlin.coroutines.Continuation"
}

val MethodInfo.unmangledName: String
    get() = name.substringBefore('-')

val MethodParameterInfo.fullTypeName: String
    get() = typeSignatureOrTypeDescriptor.fullTypeName

val TypeSignature.fullTypeName: String
    get() = when (this) {
        is ClassRefTypeSignature -> fullyQualifiedClassName
        is BaseTypeSignature -> typeStr
        is ArrayTypeSignature -> nestedType.fullTypeName
        is TypeVariableSignature -> resolve().classBound?.fullTypeName
            ?: resolve().interfaceBounds.getOrNull(0)?.fullTypeName
            ?: "java.lang.Object"

        else -> error("Unhandled $this")
    }

fun MethodInfo.toFullyQualifiedSignature(): String {
    return "$className.$name(${parameterInfo.joinToString { it.fullTypeName }})"
}
