package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.isAssignableFrom
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.internal.core.ClassPathProcessor
import io.github.freya022.botcommands.internal.parameters.resolvers.exceptions.MissingResolverAnnotation
import io.github.freya022.botcommands.internal.parameters.resolvers.exceptions.MissingResolverFactoryAnnotation
import io.github.freya022.botcommands.internal.parameters.resolvers.exceptions.MissingResolverFactorySuperclass
import io.github.freya022.botcommands.internal.parameters.resolvers.exceptions.MissingResolverSuperclass
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.shortQualifiedName
import io.github.freya022.botcommands.internal.utils.shortSignature
import java.lang.reflect.Method

// This checker works on all classes from the user packages, but only on "services" of internal classes
// Symbol processing happens on the postProcess step (using a task list) as all exclusions need to be retrieved first
internal class ResolverSupertypeChecker internal constructor(): ClassPathProcessor {
    private val tasks: MutableList<() -> Unit> = arrayListOf()

    private val missingResolverAnnotationMessages: MutableList<String> = arrayListOf()
    private val missingResolverSuperclassMessages: MutableList<String> = arrayListOf()
    private val missingResolverFactoryAnnotationMessages: MutableList<String> = arrayListOf()
    private val missingResolverFactorySuperclassMessages: MutableList<String> = arrayListOf()

    override fun processClass(data: ClassPathProcessor.ClassData) {
        val classInfo = data.classInfo
        val clazz = data.clazz
        val isService = data.isService
        if (classInfo.isAbstract) return

        val isResolverFactoryAnnotated = classInfo.hasAnnotation(ResolverFactory::class.java)
        val isResolverFactorySubclass = clazz.isSubclassOf<ParameterResolverFactory>()
        val missingResolverFactoryAnnotation = !isResolverFactoryAnnotated && isResolverFactorySubclass
                // Only check for annotation if the class is already a service
                && isService
        val missingResolverFactorySuperClass = isResolverFactoryAnnotated && !isResolverFactorySubclass

        val isResolverAnnotated = classInfo.hasAnnotation(Resolver::class.java)
        val isResolverSubclass = clazz.isSubclassOf<ParameterResolver<*, *>>()
        val missingResolverAnnotation = !isResolverAnnotated && isResolverSubclass
                // Only check for annotation if the class is already a service
                && isService
        val missingResolverSuperClass = isResolverAnnotated && !isResolverSubclass

        tasks += task@{
            if (missingResolverAnnotation) {
                missingResolverAnnotationMessages += "Resolver ${classInfo.shortQualifiedName} needs to be annotated with ${annotationRef<Resolver>()}"
            } else if (missingResolverSuperClass) {
                missingResolverSuperclassMessages += "Resolver ${classInfo.shortQualifiedName} needs to extend ${classRef<ParameterResolver<*, *>>()}"
            } else if (missingResolverFactoryAnnotation) {
                missingResolverFactoryAnnotationMessages += "Resolver factory ${classInfo.shortQualifiedName} needs to be annotated with ${annotationRef<ResolverFactory>()}"
            } else if (missingResolverFactorySuperClass) {
                missingResolverFactorySuperclassMessages += "Resolver factory ${classInfo.shortQualifiedName} needs to extend ${classRef<ParameterResolverFactory>()}"
            }
        }
    }

    override fun processMethod(data: ClassPathProcessor.MethodData) {
        val method = data.method
        if (method !is Method) return

        val methodInfo = data.methodInfo
        val isServiceFactory = data.isServiceFactory

        val isResolverFactoryAnnotated = methodInfo.hasAnnotation(ResolverFactory::class.java)
        val isReturnTypeResolverFactory = ParameterResolverFactory::class.isAssignableFrom(method.returnType)
        val missingResolverFactoryAnnotation = !isResolverFactoryAnnotated && isReturnTypeResolverFactory
                // Only check for annotation if the class is already a service
                && isServiceFactory
        val missingResolverFactorySuperClass = isResolverFactoryAnnotated && !isReturnTypeResolverFactory

        val isResolverAnnotated = methodInfo.hasAnnotation(Resolver::class.java)
        val isResolverReturnType = ParameterResolver::class.isAssignableFrom(method.returnType)
        val missingResolverAnnotation = !isResolverAnnotated && isResolverReturnType
                // Only check for annotation if the class is already a service
                && isServiceFactory
        val missingResolverSuperClass = isResolverAnnotated && !isResolverReturnType

        tasks += task@{
            if (missingResolverAnnotation) {
                missingResolverAnnotationMessages += "Resolver ${methodInfo.shortSignature} needs to be annotated with ${annotationRef<Resolver>()}"
            } else if (missingResolverSuperClass) {
                missingResolverSuperclassMessages += "Resolver ${methodInfo.shortSignature} needs to return a subclass of ${classRef<ParameterResolver<*, *>>()}"
            } else if (missingResolverFactoryAnnotation) {
                missingResolverFactoryAnnotationMessages += "Resolver factory ${methodInfo.shortSignature} needs to be annotated with ${annotationRef<ResolverFactory>()}"
            } else if (missingResolverFactorySuperClass) {
                missingResolverFactorySuperclassMessages += "Resolver factory ${methodInfo.shortSignature} needs to return a subclass of ${classRef<ParameterResolverFactory>()}"
            }
        }
    }

    override fun postProcess(data: ClassPathProcessor.PostProcessData) {
        tasks.forEach { it.invoke() }

        if (missingResolverAnnotationMessages.isNotEmpty()) {
            throw MissingResolverAnnotation('\n' + missingResolverAnnotationMessages.joinAsList())
        } else if (missingResolverFactoryAnnotationMessages.isNotEmpty()) {
            throw MissingResolverFactoryAnnotation('\n' + missingResolverFactoryAnnotationMessages.joinAsList())
        } else if (missingResolverSuperclassMessages.isNotEmpty()) {
            throw MissingResolverSuperclass('\n' + missingResolverSuperclassMessages.joinAsList())
        } else if (missingResolverFactorySuperclassMessages.isNotEmpty()) {
            throw MissingResolverFactorySuperclass('\n' + missingResolverFactorySuperclassMessages.joinAsList())
        }
    }
}
