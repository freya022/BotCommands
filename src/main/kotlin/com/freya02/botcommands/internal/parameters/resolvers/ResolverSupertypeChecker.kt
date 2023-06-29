package com.freya02.botcommands.internal.parameters.resolvers

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.core.service.ClassGraphProcessor
import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.api.core.service.annotations.ResolverFactory
import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.api.parameters.ParameterResolver
import com.freya02.botcommands.api.parameters.ParameterResolverFactory
import com.freya02.botcommands.internal.utils.shortSignature
import io.github.classgraph.ClassInfo
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

//This checker works on all classes from the user packages, but only on "services" of internal classes
class ResolverSupertypeChecker : ClassGraphProcessor {
    private val errorMessages: MutableList<String> = arrayListOf()

    override fun processClass(context: BContext, classInfo: ClassInfo, kClass: KClass<*>) {
        val isResolverAnnotated = classInfo.hasAnnotation(Resolver::class.java)
        val isResolverSubclass = kClass.isSubclassOf(ParameterResolver::class)
        if (isResolverAnnotated && !isResolverSubclass) {
            errorMessages += "Resolver ${classInfo.shortSignature} needs to extend ${ParameterResolver::class.simpleNestedName}"
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

    override fun postProcess(context: BContext) {
        if (errorMessages.isNotEmpty()) {
            throw IllegalStateException(errorMessages.joinToString(prefix = "\n - ", separator = "\n - "))
        }
    }
}