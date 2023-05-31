package com.freya02.botcommands.internal.core

import kotlin.reflect.KClass
import kotlin.reflect.KFunction

internal class ServiceProviders {
    private val map: MutableMap<String, ServiceProvider> = hashMapOf()

    internal fun putServiceProvider(kClass: KClass<*>) {
        val provider = ClassServiceProvider(kClass)
        map[provider.name] = provider
    }

    internal fun putServiceProvider(kFunction: KFunction<*>) {
        val provider = FunctionServiceProvider(kFunction)
        map[provider.name] = provider
    }

    internal fun findForType(type: KClass<*>): ServiceProvider? = map.values.find { type in it.types }

    internal fun findForName(name: String): ServiceProvider? = map[name]
}