package dev.freya02.botcommands.jda.ktx

import io.github.classgraph.*
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test

class DurationExtensionsPresenceTest {

    @Test
    fun `All JDA methods accepting Duration have extensions`() {
        val methodsWithLongParameter = ClassGraph()
            .acceptPackages("io.github.freya022.botcommands", "dev.freya02.botcommands")
            .enableMethodInfo()
            .enableAnnotationInfo()
            .scan()
            .use { scan ->
                scan.allClasses
                    .asSequence()
                    .flatMap { it.declaredMethodInfo }
                    .filter { it.parameterInfo.any { param -> param.isLongParameter() } }
                    .toList()
            }

        ClassGraph()
            .acceptPackages("net.dv8tion.jda.api")
            .enableMethodInfo()
            .scan()
            .use { scan ->
                val methodsAcceptingDuration = scan.allClasses
                    .asSequence()
                    .flatMap { it.declaredMethodInfo }
                    .filterNot { it.isStatic }
                    .filter { it.parameterInfo.any { param -> param.isJavaDurationParameter() } }
                    .toList()

                val missingKotlinDurationEquivalents = methodsAcceptingDuration.filter { method ->
                    methodsWithLongParameter.none { isKotlinDurationEquivalent(method, it) }
                }
                assertTrue(missingKotlinDurationEquivalents.isEmpty()) {
                    "Some functions miss kotlin.time.Duration equivalents:\n${missingKotlinDurationEquivalents.joinToString("\n") { "${it.classInfo.name}: $it" }}"
                }
            }
    }

    private fun MethodParameterInfo.isJavaDurationParameter(): Boolean {
        return (typeSignatureOrTypeDescriptor as? ClassRefTypeSignature)?.fullyQualifiedClassName == "java.time.Duration"
    }

    private fun MethodParameterInfo.isLongParameter(): Boolean {
        return (typeSignatureOrTypeDescriptor as? BaseTypeSignature)?.type == Long::class.javaPrimitiveType!!
    }

    private fun isKotlinDurationEquivalent(javaMethod: MethodInfo, kotlinFunction: MethodInfo): Boolean {
        return javaMethod.name == kotlinFunction.unmangledName
                && javaMethod.getJavaInvocationParameterTypes() == kotlinFunction.getKotlinInvocationParameterTypes()
    }

    private fun MethodInfo.getJavaInvocationParameterTypes(): List<String> {
        return listOf(this.className) + this.parameterInfo.map { it.fullTypeName }
    }

    private fun MethodInfo.getKotlinInvocationParameterTypes(): List<String> {
        return this.parameterInfo.map {
            val typeName = it.fullTypeName
            if (typeName == "long") {
                "java.time.Duration"
            } else {
                typeName
            }
        }
    }

    private val MethodParameterInfo.fullTypeName: String
        get() = typeSignatureOrTypeDescriptor.fullTypeName

    private val TypeSignature.fullTypeName: String
        get() = when (this) {
            is ClassRefTypeSignature -> fullyQualifiedClassName
            is BaseTypeSignature -> typeStr
            is ArrayTypeSignature -> nestedType.fullTypeName
            is TypeVariableSignature -> resolve().classBound?.fullTypeName
                ?: resolve().interfaceBounds.getOrNull(0)?.fullTypeName
                ?: "java.lang.Object"

            else -> error("Unhandled $this")
        }

    private val MethodInfo.unmangledName: String
        get() = name.substringBefore('-')
}
