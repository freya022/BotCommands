package com.freya02.botcommands.internal.parameters.resolvers

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.service.ClassGraphProcessor
import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.api.core.service.annotations.ResolverFactory
import com.freya02.botcommands.api.core.utils.joinAsList
import com.freya02.botcommands.api.core.utils.shortSignature
import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.api.parameters.ParameterResolver
import com.freya02.botcommands.api.parameters.ParameterResolverFactory
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isService
import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import java.lang.reflect.Executable
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

//This checker works on all classes from the user packages, but only on "services" of internal classes
internal class ResolverSupertypeChecker internal constructor(): ClassGraphProcessor {
    private val errorMessages: MutableList<String> = arrayListOf()

    override fun processClass(context: BContext, classInfo: ClassInfo, kClass: KClass<*>) {
        val isResolverAnnotated = classInfo.hasAnnotation(Resolver::class.java)
        val isResolverSubclass = kClass.isSubclassOf(ParameterResolver::class)
        if (isResolverAnnotated && !isResolverSubclass) {
            errorMessages += "Resolver ${classInfo.shortSignature} needs to extend ${ParameterResolver::class.simpleNestedName}"
            //TODO add early returns in all CG processors as to avoid unnecessary checks after
        } else if (!isResolverAnnotated && isResolverSubclass) {
            errorMessages +=  "Resolver ${classInfo.shortSignature} needs to be annotated with @${Resolver::class.simpleNestedName}"
        }

        val isResolverFactoryAnnotated = classInfo.hasAnnotation(ResolverFactory::class.java)
        val isResolverFactorySubclass = kClass.isSubclassOf(ParameterResolverFactory::class)
        if (isResolverFactoryAnnotated && !isResolverFactorySubclass) {
            errorMessages += "Resolver factory ${classInfo.shortSignature} needs to extend ${ParameterResolverFactory::class.simpleNestedName}"
        } else if (!isResolverFactoryAnnotated && isResolverFactorySubclass) {
            errorMessages += "Resolver factory ${classInfo.shortSignature} needs to be annotated with @${ResolverFactory::class.simpleNestedName}"
        }
    }

    override fun processMethod(
        context: BContext,
        methodInfo: MethodInfo,
        method: Executable,
        classInfo: ClassInfo,
        kClass: KClass<*>
    ) {
        if (method !is Method) return

        val isServiceAnnotated = methodInfo.isService(context.config)

        val isResolverAnnotated = methodInfo.hasAnnotation(Resolver::class.java)
        val isResolverReturnType = ParameterResolver::class.java.isAssignableFrom(method.returnType)
        if (!isResolverAnnotated && isResolverReturnType && isServiceAnnotated) {
            // Not annotated as a resolver
            errorMessages += "Resolver ${methodInfo.shortSignature} needs to be annotated with @${Resolver::class.simpleNestedName}"
            return
        } else if (isResolverAnnotated && !isResolverReturnType) {
            // Wrong return type
            errorMessages += "Resolver ${methodInfo.shortSignature} needs to return a subclass of ${ParameterResolver::class.simpleNestedName}"
            return
        }

        val isResolverFactoryAnnotated = methodInfo.hasAnnotation(ResolverFactory::class.java)
        val isResolverFactoryReturnType = ParameterResolverFactory::class.java.isAssignableFrom(method.returnType)
        if (!isResolverFactoryAnnotated && isResolverFactoryReturnType && isServiceAnnotated) {
            // Not annotated as a resolver
            errorMessages += "Resolver factory ${methodInfo.shortSignature} needs to be annotated with @${ResolverFactory::class.simpleNestedName}"
            return
        } else if (isResolverFactoryAnnotated && !isResolverFactoryReturnType) {
            // Wrong return type
            errorMessages += "Resolver factory ${methodInfo.shortSignature} needs to return a subclass of ${ParameterResolverFactory::class.simpleNestedName}"
            return
        }
    }

    override fun postProcess(context: BContext) {
        //TODO replace these patterns by "check" in all CG processors
        if (errorMessages.isNotEmpty()) {
            throw IllegalStateException('\n' + errorMessages.joinAsList())
        }
    }
}