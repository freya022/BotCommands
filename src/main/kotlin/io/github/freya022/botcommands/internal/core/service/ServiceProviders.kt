package io.github.freya022.botcommands.internal.core.service

import io.github.classgraph.ClassInfo
import io.github.classgraph.MethodInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.ClassGraphProcessor
import io.github.freya022.botcommands.internal.core.BContextImpl
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.isService
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.throwUser
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.kotlinFunction

internal class ServiceProviders : ClassGraphProcessor {
    private val nameMap: MutableMap<String, ServiceProvider> = ConcurrentHashMap()
    private val typeMap: MutableMap<KClass<*>, MutableSet<ServiceProvider>> = ConcurrentHashMap()

    internal fun putServiceProvider(serviceProvider: ServiceProvider) {
        if (serviceProvider.name in nameMap)
            throw IllegalArgumentException("Service provider for '${serviceProvider.name}' already exists (tried to insert '${serviceProvider.providerKey}', existing provider: '${nameMap[serviceProvider.name]?.providerKey}')")

        nameMap[serviceProvider.name] = serviceProvider
        serviceProvider.types.forEach { type ->
            typeMap.computeIfAbsent(type) { ConcurrentSkipListSet() }.add(serviceProvider)
        }
    }

    internal fun findAllForType(type: KClass<*>): Set<ServiceProvider> = typeMap[type] ?: emptySet()
    internal fun findForType(type: KClass<*>): ServiceProvider? = typeMap[type]?.firstOrNull()
    internal fun findForName(name: String): ServiceProvider? = nameMap[name]

    override fun processClass(context: BContext, classInfo: ClassInfo, kClass: KClass<*>) {
        context as BContextImpl

        if (classInfo.isService(context.config)) {
            context.serviceProviders.putServiceProvider(ClassServiceProvider(kClass))
        }
    }

    override fun processMethod(context: BContext, methodInfo: MethodInfo, method: Executable, classInfo: ClassInfo, kClass: KClass<*>) {
        context as BContextImpl

        if (methodInfo.isService(context.config)) {
            if (methodInfo.isConstructor)
                throwUser("Constructor of ${classInfo.simpleName} cannot be annotated with a service annotation")
            method as Method

            val function =
                method.kotlinFunction
                    ?: kClass.memberProperties.find { it.javaGetter == method }?.getter
                    ?: throwInternal("Cannot get KFunction/KProperty.Getter from $method")
            context.serviceProviders.putServiceProvider(FunctionServiceProvider(function))
        }
    }
}