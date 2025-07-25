package dev.freya02.botcommands.jda.ktx

import dev.freya02.botcommands.jda.ktx.utils.getJavaInvocationParameterTypes
import dev.freya02.botcommands.jda.ktx.utils.getKotlinInvocationParameterTypes
import dev.freya02.botcommands.jda.ktx.utils.unmangledName
import io.github.classgraph.*
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test

class DurationExtensionsPresenceTest {

    @Test
    fun `All JDA methods accepting Duration have extensions`() {
        withMethodsWithLongParameter { methodsWithLongParameter ->
            withMethodsAcceptingDuration { methodsAcceptingDuration ->
                val missingKotlinDurationEquivalents = methodsAcceptingDuration.filter { method ->
                    methodsWithLongParameter.none { isKotlinDurationEquivalent(method, it) }
                }
                assertTrue(missingKotlinDurationEquivalents.isEmpty()) {
                    "Some functions miss kotlin.time.Duration equivalents:\n${missingKotlinDurationEquivalents.joinToString("\n") { "${it.classInfo.name}: $it" }}"
                }
            }
        }
    }

    private fun withMethodsWithLongParameter(block: (methodsWithLongParameter: List<MethodInfo>) -> Unit) {
        ClassGraph()
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
                    .also(block)
            }
    }

    private fun withMethodsAcceptingDuration(block: (methodsAcceptingDuration: List<MethodInfo>) -> Unit) {
        ClassGraph()
            .acceptPackages("net.dv8tion.jda.api")
            .enableMethodInfo()
            .scan()
            .use { scan ->
                scan.allClasses
                    .asSequence()
                    .flatMap { it.declaredMethodInfo }
                    .filterNot { it.isStatic }
                    .filter { it.parameterInfo.any { param -> param.isJavaDurationParameter() } }
                    .toList()
                    .also(block)
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
                && javaMethod.getJavaInvocationParameterTypes() == kotlinFunction.getKotlinInvocationParameterTypes().replaceLongWithJavaDuration()
    }

    private fun List<String>.replaceLongWithJavaDuration() = map {
        if (it == "long") {
            "java.time.Duration"
        } else {
            it
        }
    }
}
