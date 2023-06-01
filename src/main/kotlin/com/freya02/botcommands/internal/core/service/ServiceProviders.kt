package com.freya02.botcommands.internal.core.service

import com.freya02.botcommands.api.core.service.annotations.InterfacedService
import com.freya02.botcommands.internal.simpleNestedName
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation

internal class ServiceProviders {
    private val nameMap: MutableMap<String, ServiceProvider> = ConcurrentHashMap()
    private val typeMap: MutableMap<KClass<*>, MutableList<ServiceProvider>> = ConcurrentHashMap()

    internal fun putServiceProvider(serviceProvider: ServiceProvider) {
        if (serviceProvider.name in nameMap)
            throw IllegalArgumentException("Service provider for ${serviceProvider.providerKey} already exists")
        serviceProvider.types.forEach { type ->
            if (type.findAnnotation<InterfacedService>()?.acceptMultiple == false && type in typeMap) {
                throw IllegalArgumentException("Cannot add service provider ${serviceProvider.providerKey} for type ${type.simpleNestedName} as it already exists at ${findForType(type)!!.providerKey}")
            }
        }

        nameMap[serviceProvider.name] = serviceProvider
        serviceProvider.types.forEach { type ->
            typeMap.computeIfAbsent(type) { CopyOnWriteArrayList() }.add(serviceProvider)
        }
    }

    internal fun putServiceProvider(kClass: KClass<*>) = putServiceProvider(ClassServiceProvider(kClass))
    internal fun putServiceProvider(kFunction: KFunction<*>) = putServiceProvider(FunctionServiceProvider(kFunction))

    internal fun findAllForType(type: KClass<*>): List<ServiceProvider> = typeMap[type] ?: emptyList()
    internal fun findForType(type: KClass<*>): ServiceProvider? = typeMap[type]?.firstOrNull()
    internal fun findForName(name: String): ServiceProvider? = nameMap[name]
}