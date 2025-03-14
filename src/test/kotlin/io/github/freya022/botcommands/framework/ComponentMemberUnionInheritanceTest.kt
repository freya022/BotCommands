package io.github.freya022.botcommands.framework

import io.github.classgraph.ClassGraph
import io.github.freya022.botcommands.api.core.utils.allSuperclassesAndInterfaces
import io.github.freya022.botcommands.internal.components.CustomJDAComponent
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.internal.components.AbstractComponentImpl
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

private val logger = KotlinLogging.logger { }

/**
 * Tests that our custom components inherit the same union interfaces as JDA's
 */
class ComponentMemberUnionInheritanceTest {

    @Test
    fun `Internal components should also implement their union counterparts`() {
        // Get every *internal* class extending Component
        // Check they have [[CustomJDAComponent]]
        // Get all superinterfaces of the JDA impl
        // Filter those ending with 'Union'
        // Check our custom component extends AbstractComponentImpl and implements the unions

        val bcScan = ClassGraph()
            // This is purposefully not internal.components, as there is text inputs too
            .acceptPackages("io.github.freya022.botcommands.internal")
            .enableAnnotationInfo()
            .scan()

        // Check the 'Impl' suffix if 'isFinal' isn't enough
        val customComponents = bcScan.getClassesImplementing(Component::class.java).filter { it.isFinal }
        customComponents.forEach { customComponent ->
            val customComponentAnnotation = customComponent.getAnnotationInfo(CustomJDAComponent::class.java)
                ?.loadClassAndInstantiate() as CustomJDAComponent?
                ?: fail("${customComponent.name} should be annotated with ${annotationRef<CustomJDAComponent>()}")

            val expectedUnions = customComponentAnnotation.jdaImpl.java.allSuperclassesAndInterfaces
                .filter { it.name.endsWith("Union") }
                .map { it.name }

            if (expectedUnions.isEmpty()) {
                logger.info { "No union expected on ${customComponent.name}" }
            } else {
                assertTrue(customComponent.extendsSuperclass(AbstractComponentImpl::class.java)) {
                    "${customComponent.name} must extend ${AbstractComponentImpl::class.java.name}"
                }

                val missingUnions = expectedUnions - customComponent.interfaces.mapTo(hashSetOf()) { it.name }
                assertTrue(missingUnions.isEmpty()) {
                    "${customComponent.name} is missing unions: ${missingUnions.joinToString()}"
                }

                logger.info { "Found required unions on ${customComponent.name}: ${expectedUnions.joinToString()}" }
            }
        }

        bcScan.close()
    }
}