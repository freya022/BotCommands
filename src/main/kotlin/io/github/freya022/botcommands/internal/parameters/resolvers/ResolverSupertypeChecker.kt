package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.*
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.typeOfAtOrNullOnStar
import io.github.oshai.kotlinlogging.KotlinLogging
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass
import kotlin.reflect.full.allSupertypes
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName
import kotlin.reflect.jvm.kotlinFunction

private val logger = KotlinLogging.logger { }

// This checker works on all classes from the user packages, but only on "services" of internal classes
// Symbol processing happens on the postProcess step (using a task list) as all exclusions need to be retrieved first
internal class ResolverSupertypeChecker internal constructor(): ClassGraphProcessor {
    private val tasks: MutableList<() -> Unit> = arrayListOf()
    private val ignoredClasses: MutableSet<Class<*>> = hashSetOf()
    private val errorMessages: MutableList<String> = arrayListOf()

    override fun processClass(classInfo: ClassInfo, kClass: KClass<*>, isService: Boolean) {
        if (classInfo.isAbstract) return

        val isResolverFactoryAnnotated = classInfo.hasAnnotation(ResolverFactory::class.java)
        val isResolverFactorySubclass = kClass.isSubclassOf<ParameterResolverFactory<*>>()
        val missingResolverFactoryAnnotation = !isResolverFactoryAnnotated && isResolverFactorySubclass
                // Only check for annotation if the class is already a service
                && isService
        val missingResolverFactorySuperClass = isResolverFactoryAnnotated && !isResolverFactorySubclass

        // Do not care about resolvers returned by factories given by service factories
        if (isResolverFactorySubclass) {
            val factoryType = kClass.allSupertypes.first { it.jvmErasure == ParameterResolverFactory::class }
            val factoryOutputType = factoryType.arguments[0].type!!.jvmErasure
            logger.trace { "Skipping checks of ${factoryOutputType.jvmName} as it is referenced by ${classInfo.simpleNestedName}" }
            ignoredClasses += factoryOutputType.java
        }

        val isResolverAnnotated = classInfo.hasAnnotation(Resolver::class.java)
        val isResolverSubclass = kClass.isSubclassOf<ParameterResolver<*, *>>()
        val missingResolverAnnotation = !isResolverAnnotated && isResolverSubclass
                // Only check for annotation if the class is already a service
                && isService
        val missingResolverSuperClass = isResolverAnnotated && !isResolverSubclass

        tasks += task@{
            if (missingResolverAnnotation) {
                // Skip if a factory references the resolver or one of its subtypes
                if (ignoredClasses.any { it.isAssignableFrom(kClass.java) }) return@task

                errorMessages += "Resolver ${classInfo.shortQualifiedName} needs to be annotated with ${annotationRef<Resolver>()}"
            } else if (missingResolverSuperClass) {
                errorMessages += "Resolver ${classInfo.shortQualifiedName} needs to extend ${classRef<ParameterResolver<*, *>>()}"
            } else if (missingResolverFactoryAnnotation) {
                errorMessages += "Resolver factory ${classInfo.shortQualifiedName} needs to be annotated with ${annotationRef<ResolverFactory>()}"
            } else if (missingResolverFactorySuperClass) {
                errorMessages += "Resolver factory ${classInfo.shortQualifiedName} needs to extend ${classRef<ParameterResolverFactory<*>>()}"
            }
        }
    }

    override fun processMethod(
        methodInfo: MethodInfo,
        method: Executable,
        classInfo: ClassInfo,
        kClass: KClass<*>,
        isServiceFactory: Boolean
    ) {
        if (method !is Method) return

        val isResolverFactoryAnnotated = methodInfo.hasAnnotation(ResolverFactory::class.java)
        val isReturnTypeResolverFactory = ParameterResolverFactory::class.isAssignableFrom(method.returnType)
        val missingResolverFactoryAnnotation = !isResolverFactoryAnnotated && isReturnTypeResolverFactory
                // Only check for annotation if the class is already a service
                && isServiceFactory
        val missingResolverFactorySuperClass = isResolverFactoryAnnotated && !isReturnTypeResolverFactory

        // Do not care about resolvers returned by factories given by service factories
        if (isReturnTypeResolverFactory) {
            addFactoryReturnTypeAsIgnored(method, methodInfo)
        }

        val isResolverAnnotated = methodInfo.hasAnnotation(Resolver::class.java)
        val isResolverReturnType = ParameterResolver::class.isAssignableFrom(method.returnType)
        val missingResolverAnnotation = !isResolverAnnotated && isResolverReturnType
                // Only check for annotation if the class is already a service
                && isServiceFactory
        val missingResolverSuperClass = isResolverAnnotated && !isResolverReturnType

        tasks += task@{
            if (missingResolverAnnotation) {
                // Skip if a factory references the resolver or one of its subtypes
                if (ignoredClasses.any { it.isAssignableFrom(method.returnType) }) return@task

                errorMessages += "Resolver ${methodInfo.shortSignature} needs to be annotated with ${annotationRef<Resolver>()}"
            } else if (missingResolverSuperClass) {
                errorMessages += "Resolver ${methodInfo.shortSignature} needs to return a subclass of ${classRef<ParameterResolver<*, *>>()}"
            } else if (missingResolverFactoryAnnotation) {
                errorMessages += "Resolver factory ${methodInfo.shortSignature} needs to be annotated with ${annotationRef<ResolverFactory>()}"
            } else if (missingResolverFactorySuperClass) {
                errorMessages += "Resolver factory ${methodInfo.shortSignature} needs to return a subclass of ${classRef<ParameterResolverFactory<*>>()}"
            }
        }
    }

    private fun addFactoryReturnTypeAsIgnored(method: Method, methodInfo: MethodInfo) {
        // Fast path if the return type is ParameterResolverFactory,
        // though it could have a wildcard instead.
        if (method.returnType == ParameterResolverFactory::class.java) {
            (method.genericReturnType as? ParameterizedType)?.let { returnType ->
                val factoryOutputType = returnType.actualTypeArguments[0]
                if (factoryOutputType is Class<*>) {
                    logger.trace { "Skipping checks of ${factoryOutputType.name} as it is referenced by ${methodInfo.shortSignature}" }
                    ignoredClasses += factoryOutputType
                }
            }
            // The return is here as the factory output type could be a wildcard
            return
        }

        // Slow path for subclasses
        method.kotlinFunction!!.returnType
            // Ignore if the factory has no return type (i.e., generic is a star projection)
            // example: ResolverContainer#getResolverFactoryOrNull
            .typeOfAtOrNullOnStar(0, ParameterResolverFactory::class)
            ?.jvmErasure
            ?.let { factoryOutputType ->
                logger.trace { "Skipping checks of ${factoryOutputType.jvmName} as it is referenced by ${methodInfo.shortSignature}" }
                ignoredClasses += factoryOutputType.java
            }
    }

    override fun postProcess() {
        tasks.forEach { it.invoke() }

        check(errorMessages.isEmpty()) {
            '\n' + errorMessages.joinAsList()
        }
    }
}