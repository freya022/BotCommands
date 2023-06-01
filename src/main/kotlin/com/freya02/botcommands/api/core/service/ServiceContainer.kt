package com.freya02.botcommands.api.core.service

import com.freya02.botcommands.api.core.service.annotations.InjectedService
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@InjectedService
interface ServiceContainer {
    fun <T : Any> tryGetService(name: String, requiredType: KClass<T>): ServiceResult<T>
    fun <T : Any> tryGetService(name: String, requiredType: Class<T>): ServiceResult<T> = tryGetService(name, requiredType.kotlin)
    fun <T : Any> tryGetService(clazz: KClass<T>): ServiceResult<T>
    fun <T : Any> tryGetService(clazz: Class<T>): ServiceResult<T> = tryGetService(clazz.kotlin)

    fun <T : Any> getService(name: String, requiredType: KClass<T>): T = tryGetService(name, requiredType).getOrThrow()
    fun <T : Any> getService(name: String, requiredType: Class<T>): T = getService(name, requiredType.kotlin)
    fun <T : Any> getService(clazz: KClass<T>): T = tryGetService(clazz).getOrThrow()
    fun <T : Any> getService(clazz: Class<T>): T = getService(clazz.kotlin)

    fun <T : Any> getServiceOrNull(name: String, requiredType: KClass<T>): T? = tryGetService(name, requiredType).getOrNull()
    fun <T : Any> getServiceOrNull(name: String, requiredType: Class<T>): T? = getServiceOrNull(name, requiredType.kotlin)
    fun <T : Any> getServiceOrNull(clazz: KClass<T>): T? = tryGetService(clazz).getOrNull()
    fun <T : Any> getServiceOrNull(clazz: Class<T>): T? = getServiceOrNull(clazz.kotlin)

    fun <T : Any> peekServiceOrNull(name: String, requiredType: KClass<T>): T?
    fun <T : Any> peekServiceOrNull(name: String, requiredType: Class<T>): T? = peekServiceOrNull(name, requiredType.kotlin)
    fun <T : Any> peekServiceOrNull(clazz: KClass<T>): T?
    fun <T : Any> peekServiceOrNull(clazz: Class<T>): T? = peekServiceOrNull(clazz.kotlin)

    fun <T : Any> putServiceAs(t: T, clazz: KClass<out T>, name: String? = null)
    fun <T : Any> putServiceAs(t: T, clazz: Class<out T>) = putServiceAs(t, clazz.kotlin)
    fun putService(t: Any, name: String?): Unit = putServiceAs(t, t::class, name)
    fun putService(t: Any): Unit = putServiceAs(t, t::class)
}

inline fun <reified T : Any> ServiceContainer.getService(): T = getService(T::class)

inline fun <reified T : Any> ServiceContainer.getServiceOrNull(): T? = getServiceOrNull(T::class)

inline fun <reified T : Any> ServiceContainer.putServiceAs(t: T) = putServiceAs(t, T::class)

inline fun <T, reified R : Any> ServiceContainer.lazy() = object : ReadOnlyProperty<T, R> {
    val value: R by lazy { getService(R::class) }

    override fun getValue(thisRef: T, property: KProperty<*>) = value
}