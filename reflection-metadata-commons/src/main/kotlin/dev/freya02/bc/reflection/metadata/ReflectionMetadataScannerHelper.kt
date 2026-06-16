package dev.freya02.bc.reflection.metadata

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo

class ReflectionMetadataScannerHelper(
    private val isService: (ClassInfo) -> Boolean,
    private val isServiceFactory: (MethodInfo) -> Boolean,
) {

    fun filterLibraryClasses(classes: Collection<ClassInfo>): List<ClassInfo> {
        // Get types referenced by factories so we get metadata from those as well
        val referencedTypes = classes.asSequence()
            .flatMap { it.methodInfo }
            .filter { isServiceFactory(it) }
            .mapTo(hashSetOf()) { it.typeDescriptor.resultType.toString() }

        return classes.filter { classInfo -> filterLibraryClass(classInfo, referencedTypes) }
    }

    private fun filterLibraryClass(classInfo: ClassInfo, referencedTypes: Set<String>): Boolean {
        if (classInfo.isServiceOrHasFactories())
            return true

        val interfaces = classInfo.interfaces
        // Get metadata from all classes that extend a referenced type
        // As we can't know exactly what object a factory could return
        val superclasses = (classInfo.superclasses + interfaces + classInfo).mapTo(hashSetOf()) { it.name }
        if (superclasses.containsAny(referencedTypes))
            return true

        if (classInfo.outerClasses.any { it.isServiceOrHasFactories() })
            return true
        if (classInfo.hasAnnotation(CONDITION_ANNOTATION))
            return true

        if (interfaces.any { it.name == CONDITIONAL_SERVICE_CHECKER_ANNOTATION || it.name == CUSTOM_CONDITION_CHECKER_ANNOTATION })
            return true

        return false
    }

    private fun ClassInfo.isServiceOrHasFactories(): Boolean {
        return isService(this) || methodInfo.any { isServiceFactory(it) }
    }

    private fun <T> Set<T>.containsAny(elements: Set<T>): Boolean = elements.any { it in this }

    companion object {
        private val lowercaseInnerClassRegex = Regex("\\$[a-z]")

        const val BSERVICE_ANNOTATION = "io.github.freya022.botcommands.api.core.service.annotations.BService"
        private const val CONDITION_ANNOTATION = "io.github.freya022.botcommands.api.core.service.annotations.Condition"
        private const val CONDITIONAL_SERVICE_CHECKER_ANNOTATION = "io.github.freya022.botcommands.api.core.service.ConditionalServiceChecker"
        private const val CUSTOM_CONDITION_CHECKER_ANNOTATION = "io.github.freya022.botcommands.api.core.service.CustomConditionChecker"

        val classNamesToCheck = listOf(
            BSERVICE_ANNOTATION,
            CONDITION_ANNOTATION,
            CONDITIONAL_SERVICE_CHECKER_ANNOTATION,
            CUSTOM_CONDITION_CHECKER_ANNOTATION,
        )

        fun filterClasses(classes: Collection<ClassInfo>, onFileFacade: (ClassInfo) -> Unit): List<ClassInfo> = classes.filter { classInfo ->
            classInfo.annotationInfo.directOnly()["kotlin.Metadata"]?.let { annotationInfo ->
                //Only keep classes, not others such as file facades
                val kind = annotationInfo.parameterValues["k"].value as Int
                if (kind == 2) { // File facade
                    onFileFacade(classInfo)
                    return@filter false
                } else if (kind != 1) { // Class
                    return@filter false
                }
            }

            if (lowercaseInnerClassRegex.containsMatchIn(classInfo.name)) return@filter false
            return@filter !classInfo.isSynthetic && !classInfo.isEnum && !classInfo.isRecord
        }
    }
}
