package dev.freya02.bc.classlist.generator

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import java.io.File

object ClassListGenerator {

    private val lowercaseInnerClassRegex = Regex("\\$[a-z]")

    // TODO make tests that depend on the core module to check that those exists
    private const val BSERVICE_ANNOTATION = "io.github.freya022.botcommands.api.core.service.annotations.BService"
    private const val CONDITION_ANNOTATION = "io.github.freya022.botcommands.api.core.service.annotations.Condition"
    private const val CONDITIONAL_SERVICE_CHECKER_ANNOTATION = "io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker"
    private const val CUSTOM_CONDITION_CHECKER_ANNOTATION = "io.github.freya022.botcommands.api.core.service.CustomConditionChecker"

    private const val COMPONENT_ANNOTATION_NAME = "org.springframework.stereotype.Component"
    private const val BEAN_ANNOTATION_NAME = "org.springframework.context.annotation.Bean"

    fun generate(buildDirs: List<File>, classpath: List<File>): String {
        // We've set CG's classpath to the entire "compileClasspath" configuration,
        //   but we only need to make a filtered class list of the current project,
        //   so we can tell CG to only look at classes that we can find in the project's file tree
        //   while allowing CG to resolve necessary stuff from dependencies, like meta annotations
        //   which are crucial for this task to work.
        val builtClassNames = buildDirs.asSequence()
            .flatMap { buildDir ->
                buildDir.walk()
                    .filter { it.extension == "class" }
                    .map { it.toRelativeString(buildDir) }
            }
            .map { it.replace('/', '.').removeSuffix(".class") }
            .toList()

        ClassGraph()
            // Classpath includes compiled project classes + dependencies (required for CG to discover all meta-annotations)
            .overrideClasspath(buildDirs + classpath)
            // Only compiled project classes
            .acceptClasses(*builtClassNames.toTypedArray())
            .enableClassInfo()
            .enableMethodInfo()
            .enableAnnotationInfo()
            .disableModuleScanning()
            .scan().use { scanResult ->
                return scanResult.allClasses
                    .filterClasses()
                    .filterLibraryClasses()
                    .joinToString("\n") { it.name }
            }
    }

    private fun List<ClassInfo>.filterLibraryClasses(): List<ClassInfo> {
        // Get types referenced by factories so we get metadata from those as well
        val referencedTypes = asSequence()
            .flatMap { it.methodInfo }
            .filter(::isServiceFactory)
            .mapTo(hashSetOf()) { it.typeDescriptor.resultType.toString() }

        return filter { classInfo -> filterLibraryClass(classInfo, referencedTypes) }
    }

    private fun filterLibraryClass(classInfo: ClassInfo, referencedTypes: Set<String>): Boolean {
        if (classInfo.isServiceOrHasFactories())
            return true

        val interfaces = classInfo.interfaces
        val allClasses = classInfo.superclasses + interfaces + classInfo
        // Get metadata from all classes that extend a referenced type
        // As we can't know exactly what object a factory could return
        if (allClasses.mapTo(hashSetOf()) { it.name }.containsAny(referencedTypes))
            return true

        if (classInfo.outerClasses.any { it.isServiceOrHasFactories() })
            return true
        if (classInfo.isAnnotation && classInfo.hasAnnotation(CONDITION_ANNOTATION))
            return true
        if (interfaces.any { it.name == CONDITIONAL_SERVICE_CHECKER_ANNOTATION || it.name == CUSTOM_CONDITION_CHECKER_ANNOTATION })
            return true

        return false
    }

    private fun ClassInfo.isServiceOrHasFactories(): Boolean {
        return isService(this) || methodInfo.any(::isServiceFactory)
    }

    // TODO try to find a way to make this more maintainable
    private fun List<ClassInfo>.filterClasses(): List<ClassInfo> = filter {
        it.annotationInfo.directOnly()["kotlin.Metadata"]?.let { annotationInfo ->
            //Only keep classes, not others such as file facades
            val kind = annotationInfo.parameterValues["k"].value as Int
            if (kind != 1) { // Class
                return@filter false
            }
        }

        if (lowercaseInnerClassRegex.containsMatchIn(it.name)) return@filter false
        return@filter !it.isSynthetic && !it.isEnum && !it.isRecord
    }

    private fun <T> Iterable<T>.containsAny(elements: Iterable<T>): Boolean = elements.any { it in this }

    private fun isService(classInfo: ClassInfo): Boolean {
        return classInfo.hasAnnotation(BSERVICE_ANNOTATION) || classInfo.hasAnnotation(COMPONENT_ANNOTATION_NAME)
    }

    private fun isServiceFactory(methodInfo: MethodInfo): Boolean {
        return methodInfo.hasAnnotation(BSERVICE_ANNOTATION) || methodInfo.hasAnnotation(BEAN_ANNOTATION_NAME)
    }
}
