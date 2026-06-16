package dev.freya02.bc.classlist.generator

import dev.freya02.bc.reflection.metadata.ReflectionMetadataScannerHelper
import dev.freya02.bc.reflection.metadata.ReflectionMetadataScannerHelper.Companion.BSERVICE_ANNOTATION
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import java.io.File

object ClassListGenerator {

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

        val helper = ReflectionMetadataScannerHelper(::isService, ::isServiceFactory)

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
                return ReflectionMetadataScannerHelper
                    .filterClasses(scanResult.allClasses, onFileFacade = { /* noop */ })
                    .let(helper::filterLibraryClasses)
                    .joinToString("\n") { it.name }
            }
    }

    private fun isService(classInfo: ClassInfo): Boolean {
        return classInfo.hasAnnotation(BSERVICE_ANNOTATION) || classInfo.hasAnnotation(COMPONENT_ANNOTATION_NAME)
    }

    private fun isServiceFactory(methodInfo: MethodInfo): Boolean {
        return methodInfo.hasAnnotation(BSERVICE_ANNOTATION) || methodInfo.hasAnnotation(BEAN_ANNOTATION_NAME)
    }
}
