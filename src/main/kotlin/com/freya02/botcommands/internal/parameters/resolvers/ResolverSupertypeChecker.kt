package com.freya02.botcommands.internal.parameters.resolvers

import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.Resolver
import com.freya02.botcommands.api.core.service.annotations.ResolverFactory
import com.freya02.botcommands.api.parameters.ParameterResolver
import com.freya02.botcommands.api.parameters.ParameterResolverFactory
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.throwUser
import kotlin.reflect.full.isSubclassOf

@BService
class ResolverSupertypeChecker(context: BContextImpl) {
    init {
        context.instantiableServiceAnnotationsMap
            .getInstantiableClassesWithAnnotation<Resolver>()
            .forEach { clazz ->
                if (!clazz.isSubclassOf(ParameterResolver::class))
                    throwUser("Resolver ${clazz.simpleNestedName} needs to extend ${ParameterResolver::class.simpleNestedName}")
            }

        context.instantiableServiceAnnotationsMap
            .getInstantiableClassesWithAnnotation<ResolverFactory>()
            .forEach { clazz ->
                if (!clazz.isSubclassOf(ParameterResolverFactory::class))
                    throwUser("Resolver factory ${clazz.simpleNestedName} needs to extend ${ParameterResolverFactory::class.simpleNestedName}")
            }
    }
}