package io.github.freya022.botcommands.internal.parameters.resolvers

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.api.core.service.annotations.Resolver
import io.github.freya022.botcommands.api.core.service.annotations.ResolverFactory
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.api.core.utils.shortQualifiedReference
import io.github.freya022.botcommands.api.core.utils.shortSignature
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.ParameterResolverFactory
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.classRef
import java.lang.reflect.Executable
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

//This checker works on all classes from the user packages, but only on "services" of internal classes
internal class ResolverSupertypeChecker internal constructor(): ClassGraphProcessor {
    private val errorMessages: MutableList<String> = arrayListOf()

    override fun processClass(context: BContext, classInfo: ClassInfo, kClass: KClass<*>, isService: Boolean) {
        val isResolverAnnotated = classInfo.hasAnnotation(Resolver::class.java)
        val isResolverSubclass = kClass.isSubclassOf(ParameterResolver::class)
        if (isResolverAnnotated && !isResolverSubclass) {
            errorMessages += "Resolver ${classInfo.shortQualifiedReference} needs to extend ${classRef<ParameterResolver<*, *>>()}"
            return
        } else if (!isResolverAnnotated && isResolverSubclass) {
            errorMessages +=  "Resolver ${classInfo.shortQualifiedReference} needs to be annotated with ${annotationRef<Resolver>()}"
            return
        }

        val isResolverFactoryAnnotated = classInfo.hasAnnotation(ResolverFactory::class.java)
        val isResolverFactorySubclass = kClass.isSubclassOf(ParameterResolverFactory::class)
        if (isResolverFactoryAnnotated && !isResolverFactorySubclass) {
            errorMessages += "Resolver factory ${classInfo.shortQualifiedReference} needs to extend ${classRef<ParameterResolverFactory<*, *>>()}"
            return
        } else if (!isResolverFactoryAnnotated && isResolverFactorySubclass) {
            errorMessages += "Resolver factory ${classInfo.shortQualifiedReference} needs to be annotated with ${annotationRef<ResolverFactory>()}"
            return
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

        val isResolverAnnotated = methodInfo.hasAnnotation(Resolver::class.java)
        val isResolverReturnType = ParameterResolver::class.java.isAssignableFrom(method.returnType)
        if (!isResolverAnnotated && isResolverReturnType && isServiceFactory) {
            // Not annotated as a resolver
            errorMessages += "Resolver ${methodInfo.shortSignature} needs to be annotated with ${annotationRef<Resolver>()}"
            return
        } else if (isResolverAnnotated && !isResolverReturnType) {
            // Wrong return type
            errorMessages += "Resolver ${methodInfo.shortSignature} needs to return a subclass of ${classRef<ParameterResolver<*, *>>()}"
            return
        }

        val isResolverFactoryAnnotated = methodInfo.hasAnnotation(ResolverFactory::class.java)
        val isResolverFactoryReturnType = ParameterResolverFactory::class.java.isAssignableFrom(method.returnType)
        if (!isResolverFactoryAnnotated && isResolverFactoryReturnType && isServiceFactory) {
            // Not annotated as a resolver
            errorMessages += "Resolver factory ${methodInfo.shortSignature} needs to be annotated with ${annotationRef<ResolverFactory>()}"
            return
        } else if (isResolverFactoryAnnotated && !isResolverFactoryReturnType) {
            // Wrong return type
            errorMessages += "Resolver factory ${methodInfo.shortSignature} needs to return a subclass of ${classRef<ParameterResolverFactory<*, *>>()}"
            return
        }
    }

    override fun postProcess(context: BContext) {
        if (errorMessages.isNotEmpty()) {
            throw IllegalStateException('\n' + errorMessages.joinAsList())
        }
    }
}