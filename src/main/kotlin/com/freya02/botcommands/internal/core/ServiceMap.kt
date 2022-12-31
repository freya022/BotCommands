package com.freya02.botcommands.internal.core

import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.throwUser
import kotlin.reflect.KClass

@PublishedApi
internal class ServiceMap {
    private val map: MutableMap<KClass<*>, Any> = hashMapOf()

    operator fun get(clazz: KClass<*>) = map[clazz]

    fun put(instance: Any, asType: KClass<*> = instance::class) {
        if (asType in map)
            throwUser("Cannot put service ${asType.simpleNestedName} as it already exists")
        if (!asType.isInstance(instance))
            throwUser("Service ${instance::class.simpleNestedName} has its service type set to ${asType.simpleNestedName} but does not extend/implement it")

        map[asType] = instance
    }

    operator fun contains(clazz: KClass<*>) = clazz in map
}