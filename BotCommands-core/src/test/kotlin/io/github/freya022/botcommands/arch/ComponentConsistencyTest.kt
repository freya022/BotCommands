package io.github.freya022.botcommands.arch

import io.github.classgraph.*
import net.dv8tion.jda.api.components.Component
import kotlin.test.Test
import kotlin.test.fail

class ComponentConsistencyTest {

    @Test
    fun `Test components override super methods if they return their declaring class`() {
        val messages = arrayListOf<String>()

        ClassGraph()
            .acceptPackages("io.github.freya022.botcommands", "dev.freya02.botcommands")
            .rejectPackages("*.internal.*")
            .enableMethodInfo()
            .scan()
            .use { scan ->
                for (componentInfo in scan.getClassesImplementing(Component::class.java)) {
                    val supertypeMethodsReturningDeclClass = componentInfo.allSupertypes
                        .flatMap { it.methodInfo }
                        .filter { !it.isStatic }
                        .filter { it.isRootDeclaration() }
                        .filter { (it.typeSignatureOrTypeDescriptor.resultType as? ClassRefTypeSignature)?.classInfo == it.classInfo }

                    for (supertypeMethodReturningDeclClass in supertypeMethodsReturningDeclClass) {
                        val declaredMethod = componentInfo.getDeclaredMethodInfo(supertypeMethodReturningDeclClass.name)
                            .find { it.getParameterTypes() == supertypeMethodReturningDeclClass.getParameterTypes() }

                        if (declaredMethod == null) {
                            messages += "${componentInfo.name} does not override ${supertypeMethodReturningDeclClass.className}#${supertypeMethodReturningDeclClass.name}"
                            continue
                        }

                        val resultType = declaredMethod.typeSignatureOrTypeDescriptor.resultType as? ClassRefTypeSignature
                        if (resultType?.isClassOrNothing(componentInfo) != true) {
                            messages += "${declaredMethod.className}#${declaredMethod.name} must override return type with ${declaredMethod.className}"
                            continue
                        }
                    }
                }
            }

        if (messages.isNotEmpty()) {
            fail(messages.joinToString("\n"))
        }
    }

    private val ClassInfo.allSupertypes: List<ClassInfo>
        get() = superclasses + interfaces

    private fun MethodInfo.isRootDeclaration(): Boolean {
        return classInfo.allSupertypes
            .flatMap { it.methodInfo }
            .none { it.name == this.name && it.getParameterTypes() == this.getParameterTypes() }
    }

    private fun MethodInfo.getParameterTypes(): List<TypeSignature> {
        return parameterInfo.map { it.typeSignatureOrTypeDescriptor }
    }

    private fun ClassRefTypeSignature.isClassOrNothing(classInfo: ClassInfo): Boolean {
        return this.classInfo == classInfo || this.baseClassName == "java.lang.Void"
    }
}
