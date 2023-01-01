package com.freya02.botcommands.internal.core

import com.freya02.botcommands.api.core.annotations.ServiceType
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.throwUser
import kotlin.reflect.KClass
import kotlin.reflect.full.hasAnnotation

@PublishedApi
internal class ServiceMap {
    private val map: MutableMap<KClass<*>, Any> = hashMapOf()

    operator fun get(clazz: KClass<*>) = map[clazz]

    fun put(instance: Any, asType: KClass<*>) {
        if (asType in map)
            throwUser("Cannot put service ${asType.simpleNestedName} as it already exists")
        if (!asType.isInstance(instance)) {
            if (instance::class.hasAnnotation<ServiceType>()) {
                throwUser("Service ${instance::class.simpleNestedName} has its service type set to ${asType.simpleNestedName} but does not extend/implement it")
            } else { //Instance suppliers are type checked before executing anyway, it's probably a dynamic problem
                throwUser("Tried to put ${instance::class.simpleNestedName} as a service of type ${asType.simpleNestedName}, but it is not an instance of that type, you should probably check your dynamic suppliers")
            }
        }

        map[asType] = instance
    }

    operator fun contains(clazz: KClass<*>) = clazz in map
}