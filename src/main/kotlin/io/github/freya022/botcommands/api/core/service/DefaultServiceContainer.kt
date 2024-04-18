package io.github.freya022.botcommands.api.core.service

import io.github.freya022.botcommands.internal.core.service.provider.getServiceName
import kotlin.reflect.KClass

interface DefaultServiceContainer : ServiceContainer {
    fun <T : Any> putService(
        t: T,
        clazz: KClass<out T>,
        name: String = clazz.getServiceName(),
        isPrimary: Boolean = false,
        priority: Int = 0,
        typeAliases: Set<KClass<*>> = emptySet()
    )
    override fun <T : Any> putServiceAs(t: T, clazz: KClass<out T>, name: String) = putService(t, clazz, name)
    override fun <T : Any> putServiceAs(t: T, clazz: KClass<out T>) = putService(t, clazz)
    override fun <T : Any> putServiceAs(t: T, clazz: Class<out T>) = putService(t, clazz.kotlin)

    override fun putService(t: Any, name: String): Unit = putService(t, t::class, name)
    override fun putService(t: Any): Unit = putService(t, t::class)
}

inline fun <reified T : Any> DefaultServiceContainer.putServiceAs(
    t: T,
    name: String = T::class.getServiceName(),
    isPrimary: Boolean = false,
    priority: Int = 0,
    typeAliases: Set<KClass<*>> = emptySet()
) = putService(t, T::class, name, isPrimary, priority, typeAliases)

inline fun <reified A : Any> DefaultServiceContainer.putServiceWithTypeAlias(
    t: Any,
    name: String = t::class.getServiceName(),
    isPrimary: Boolean = false,
    priority: Int = 0
) = putService(t, t::class, name, isPrimary, priority, setOf(A::class))