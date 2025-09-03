package io.github.freya022.botcommands.framework

import io.github.classgraph.ClassGraph
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.internal.components.AbstractComponentImpl
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests that our custom components inherit the same union interfaces as JDA's
 */
class ComponentMemberUnionInheritanceTest {

    @Test
    fun `Component should implement their unions`() {
        // 1. Take all non-abstract components BC
        // 2. Take all interfaces
        // 3. Add "Union" to them
        // 4. Check if each of them actually exists in the same package
        // 5. If it does, assert BC component has it
        ClassGraph()
            // This is purposefully not internal.components, as there is text inputs too
            .acceptPackages("io.github.freya022.botcommands.internal")
            .scan()
            .use { scan ->
                // 1.
                scan.getClassesImplementing(Component::class.java)
                    .filter { !it.isAbstract }
                    .forEach { bcComponentClass ->
                        val missingUnionMessages = arrayListOf<String>()

                        if (!bcComponentClass.extendsSuperclass(AbstractComponentImpl::class.java)) {
                            missingUnionMessages += "${bcComponentClass.name} must extend ${AbstractComponentImpl::class.java.name}"
                        }

                        // 2.
                        bcComponentClass.interfaces.forEach { superinterface ->
                            // 3.
                            val unionName = superinterface.name + "Union"

                            // 4.
                            if (javaClass.getResource("/${unionName.replace(".", "/")}.class") == null) return@forEach

                            // 5.
                            if (!bcComponentClass.implementsInterface(unionName)) {
                                missingUnionMessages += "${bcComponentClass.name} is missing $unionName"
                            }
                        }

                        assertTrue(missingUnionMessages.isEmpty(), "Some components are missing unions:\n${missingUnionMessages.joinToString("\n")}")
                    }
            }
    }
}
