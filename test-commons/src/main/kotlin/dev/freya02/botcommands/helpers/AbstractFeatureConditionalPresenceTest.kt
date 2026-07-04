package dev.freya02.botcommands.helpers

import io.github.classgraph.ClassGraph
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.hasAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmName
import kotlin.test.fail

abstract class AbstractFeatureConditionalPresenceTest {
    protected fun checkForMissingFeatureConditions(requiredAnnotation: KClass<out Annotation>) {
        ClassGraph()
            // Ignore test classes
            .filterClasspathElements { !it.endsWith("test") }
            // Ignore other modules
            .disableJarScanning()
            .disableModuleScanning()
            .disableNestedJarScanning()
            .enableClassInfo()
            .scan()
            .use { scan ->
                val classes = scan.allStandardClasses
                val kotlinClasses = classes.map { it.loadClass().kotlin }

                val missingConditions = arrayListOf<String>()
                for (klass in kotlinClasses) {
                    if (klass.hasAnnotationRecursive<BService>()) {
                        if (!klass.hasAnnotationRecursive(requiredAnnotation)) {
                            missingConditions.add(klass.jvmName)
                        }
                        // Has feature gate, no need to check functions
                    } else {
                        // Class/object isn't a service, but could contain a service factory, in which case it needs the annotation
                        for (function in klass.declaredMemberFunctions + klass.declaredMemberProperties.map { it.getter }) {
                            if (!function.hasAnnotationRecursive<BService>()) {
                                continue
                            }

                            if (!function.hasAnnotationRecursive(requiredAnnotation)) {
                                missingConditions.add(function.toString())
                            }
                        }
                    }
                }

                if (missingConditions.isNotEmpty()) {
                    fail("Some services are missing @${requiredAnnotation.simpleNestedName}:\n${missingConditions.joinAsList()}")
                }
            }
    }
}
