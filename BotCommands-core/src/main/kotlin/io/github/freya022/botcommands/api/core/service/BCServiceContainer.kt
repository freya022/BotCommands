package io.github.freya022.botcommands.api.core.service

import kotlin.reflect.KClass

interface BCServiceContainer : ServiceContainer {
    fun <T : Any> putService(
        t: T,
        clazz: KClass<out T>,
        name: String? = null,
        isPrimary: Boolean = false,
        priority: Int = 0,
        annotations: Collection<Annotation> = emptySet(),
        typeAliases: Set<KClass<*>> = emptySet()
    )
    fun <T : Any> putService(
        t: T,
        clazz: Class<out T>,
        name: String?,
        isPrimary: Boolean,
        priority: Int,
        annotations: Collection<Annotation>,
        typeAliases: Set<Class<*>>,
    ) = putService(t, clazz.kotlin, name, isPrimary, priority, annotations, typeAliases.mapTo(hashSetOf()) { it.kotlin })
    override fun <T : Any> putServiceAs(t: T, clazz: KClass<out T>, name: String) = putService(t, clazz, name)
    override fun <T : Any> putServiceAs(t: T, clazz: KClass<out T>) = putService(t, clazz)

    override fun putService(t: Any, name: String): Unit = putService(t, t::class, name)
    override fun putService(t: Any): Unit = putService(t, t::class)

    fun putSuppliedService(serviceSupplier: ServiceSupplier<*>)
}

inline fun <reified T : Any> BCServiceContainer.putServiceAs(
    t: T,
    name: String? = null,
    isPrimary: Boolean = false,
    priority: Int = 0,
    typeAliases: Set<KClass<*>> = emptySet(),
    annotations: Collection<Annotation> = emptySet(),
) = putService(t, T::class, name, isPrimary, priority, annotations, typeAliases)

inline fun <reified A : Any> BCServiceContainer.putServiceWithTypeAlias(
    t: Any,
    name: String? = null,
    isPrimary: Boolean = false,
    priority: Int = 0,
    annotations: Collection<Annotation> = emptySet(),
) = putService(t, t::class, name, isPrimary, priority, annotations, setOf(A::class))
