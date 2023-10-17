package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.reflect.Executable
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

private val logger = KotlinLogging.logger { }

// This checker works on all classes from the user packages, but only on "services" of internal classes
// Symbol processing happens on the postProcess step (using a task list) as all exclusions need to be retrieved first
internal class ResolverSupertypeChecker internal constructor(): ClassGraphProcessor {
    private val tasks: MutableList<() -> Unit> = arrayListOf()
    private val ignoredClasses: MutableSet<Class<*>> = hashSetOf()
    private val errorMessages: MutableList<String> = arrayListOf()

    override fun processClass(context: BContext, classInfo: ClassInfo, kClass: KClass<*>, isService: Boolean) {
        val isResolverFactoryAnnotated = classInfo.hasAnnotation(ResolverFactory::class.java)
        val isResolverFactorySubclass = kClass.isSubclassOf(ParameterResolverFactory::class)
        val missingResolverFactoryAnnotation = isResolverFactoryAnnotated && !isResolverFactorySubclass
        val missingResolverFactorySuperClass = !isResolverFactoryAnnotated && isResolverFactorySubclass

        if (isResolverFactorySubclass) {
            val factoryType = kClass.allSupertypes.first { it.jvmErasure == ParameterResolverFactory::class }
            val factoryOutputType = factoryType.arguments[0].type!!.jvmErasure
            logger.trace { "Skipping checks of ${factoryOutputType.jvmName} as it is referenced by ${classInfo.simpleNestedName}" }
            ignoredClasses += factoryOutputType.java
        }

        val isResolverAnnotated = classInfo.hasAnnotation(Resolver::class.java)
        val isResolverSubclass = kClass.isSubclassOf(ParameterResolver::class)
        val missingResolverAnnotation = !isResolverAnnotated && isResolverSubclass
        val missingResolverSuperClass = isResolverAnnotated && !isResolverSubclass

        tasks += task@{
            if (missingResolverAnnotation) {
                // Skip if a factory references the resolver
                if (kClass.java in ignoredClasses) return@task
                if (kClass.java.allSuperclasses.any { it in ignoredClasses }) return@task

                errorMessages += "Resolver ${classInfo.shortQualifiedReference} needs to be annotated with ${annotationRef<Resolver>()}"
                return@task
            } else if (missingResolverSuperClass) {
                errorMessages += "Resolver ${classInfo.shortQualifiedReference} needs to extend ${classRef<ParameterResolver<*, *>>()}"
                return@task
            }

            if (missingResolverFactoryAnnotation) {
                errorMessages += "Resolver factory ${classInfo.shortQualifiedReference} needs to be annotated with ${annotationRef<ResolverFactory>()}"
                return@task
            } else if (missingResolverFactorySuperClass) {
                errorMessages += "Resolver factory ${classInfo.shortQualifiedReference} needs to extend ${classRef<ParameterResolverFactory<*>>()}"
                return@task
            }
        }
    }

    override fun processMethod(
        context: BContext,
        methodInfo: MethodInfo,
        method: Executable,
        classInfo: ClassInfo,
        kClass: KClass<*>,
        isServiceFactory: Boolean
    ) {
        if (method !is Method) return

        val isResolverFactoryAnnotated = methodInfo.hasAnnotation(ResolverFactory::class.java)
        val isReturnTypeResolverFactory = ParameterResolverFactory::class.java.isAssignableFrom(method.returnType)
        val missingResolverFactoryAnnotation = !isResolverFactoryAnnotated && isReturnTypeResolverFactory && isServiceFactory
        val missingResolverFactorySuperClass = isResolverFactoryAnnotated && !isReturnTypeResolverFactory

        if (isServiceFactory && isReturnTypeResolverFactory) {
            val factoryType = method.returnType.kotlin.allSupertypes.first { it.jvmErasure == ParameterResolverFactory::class }
            val factoryOutputType = factoryType.arguments[0].type!!.jvmErasure
            logger.trace { "Skipping checks of ${factoryOutputType.jvmName} as it is referenced by ${methodInfo.shortSignature}" }
            ignoredClasses += factoryOutputType.java
        }

        val isResolverAnnotated = methodInfo.hasAnnotation(Resolver::class.java)
        val isResolverReturnType = ParameterResolver::class.java.isAssignableFrom(method.returnType)
        val missingResolverAnnotation = !isResolverAnnotated && isResolverReturnType && isServiceFactory
        val missingResolverSuperClass = isResolverAnnotated && !isResolverReturnType

        tasks += task@{
            if (missingResolverAnnotation) {
                // Skip if a factory references the resolver
                if (method.returnType in ignoredClasses) return@task
                if (method.returnType.allSuperclasses.any { it in ignoredClasses }) return@task

                errorMessages += "Resolver ${methodInfo.shortSignature} needs to be annotated with ${annotationRef<Resolver>()}"
                return@task
            } else if (missingResolverSuperClass) {
                errorMessages += "Resolver ${methodInfo.shortSignature} needs to return a subclass of ${classRef<ParameterResolver<*, *>>()}"
                return@task
            }

            if (missingResolverFactoryAnnotation) {
                errorMessages += "Resolver factory ${methodInfo.shortSignature} needs to be annotated with ${annotationRef<ResolverFactory>()}"
                return@task
            } else if (missingResolverFactorySuperClass) {
                errorMessages += "Resolver factory ${methodInfo.shortSignature} needs to return a subclass of ${classRef<ParameterResolverFactory<*>>()}"
                return@task
            }
        }
    }

    override fun postProcess(context: BContext) {
        tasks.forEach { it.invoke() }

        if (errorMessages.isNotEmpty()) {
            throw IllegalStateException('\n' + errorMessages.joinAsList())
        }
    }
}