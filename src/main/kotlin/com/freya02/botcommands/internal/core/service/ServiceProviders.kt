package com.freya02.botcommands.internal.core.service

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

internal class ServiceProviders {
    private val map: MutableMap<String, ServiceProvider> = ConcurrentHashMap()

    internal fun putServiceProvider(serviceProvider: ServiceProvider) {
        if (serviceProvider.name in map)
            throw IllegalArgumentException("Service provider for ${serviceProvider.providerKey} already exists")
        map[serviceProvider.name] = serviceProvider
    }

    internal fun putServiceProvider(kClass: KClass<*>) = putServiceProvider(ClassServiceProvider(kClass))
    internal fun putServiceProvider(kFunction: KFunction<*>) = putServiceProvider(FunctionServiceProvider(kFunction))

    internal fun findAllForType(type: KClass<*>): List<ServiceProvider> = map.values.filter { type in it.types }
    internal fun findForType(type: KClass<*>): ServiceProvider? = map.values.find { type in it.types }
    internal fun findForName(name: String): ServiceProvider? = map[name]
}