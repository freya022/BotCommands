package io.github.freya022.botcommands.api.core.service

import kotlin.reflect.KClass

interface DefaultServiceContainer : ServiceContainer {
    fun <T : Any> putService(
        t: T,
        clazz: KClass<out T>,
        name: String? = null,
        isPrimary: Boolean = false,
        priority: Int = 0,
        annotations: Collection<Annotation> = emptySet(),
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
    name: String? = null,
    isPrimary: Boolean = false,
    priority: Int = 0,
    typeAliases: Set<KClass<*>> = emptySet(),
    annotations: Collection<Annotation> = emptySet(),
) = putService(t, T::class, name, isPrimary, priority, annotations, typeAliases)

inline fun <reified A : Any> DefaultServiceContainer.putServiceWithTypeAlias(
    t: Any,
    name: String? = null,
    isPrimary: Boolean = false,
    priority: Int = 0,
    annotations: Collection<Annotation> = emptySet(),
) = putService(t, t::class, name, isPrimary, priority, annotations, setOf(A::class))