package io.github.freya022.botcommands.api.core.service

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@InjectedService
interface ServiceContainer {
    fun <T : Any> tryGetService(name: String, requiredType: KClass<T>): ServiceResult<T>
    fun <T : Any> tryGetService(name: String, requiredType: Class<T>): ServiceResult<T> = tryGetService(name, requiredType.kotlin)
    fun <T : Any> tryGetService(clazz: KClass<T>): ServiceResult<T>
    fun <T : Any> tryGetService(clazz: Class<T>): ServiceResult<T> = tryGetService(clazz.kotlin)

    fun canCreateService(clazz: KClass<*>): ServiceError?
    fun canCreateService(clazz: Class<*>): ServiceError? = canCreateService(clazz.kotlin)

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

    fun <T : Any> getInterfacedServiceTypes(clazz: KClass<T>): List<KClass<T>>
    fun <T : Any> getInterfacedServiceTypes(clazz: Class<T>): List<Class<T>> =
        getInterfacedServiceTypes(clazz.kotlin).map { it.java }

    /**
     * Filters out interfaced services of that type if they are already being inspected.
     *
     * This allows you to check other implementations of your own interfaced service, without having a circular dependency.
     */
    fun <T : Any> getInterfacedServices(clazz: KClass<T>): List<T>
    /**
     * Filters out interfaced services of that type if they are already being inspected.
     *
     * This allows you to check other implementations of your own interfaced service, without having a circular dependency.
     */
    fun <T : Any> getInterfacedServices(clazz: Class<T>): List<T> =
        getInterfacedServices(clazz.kotlin)

    fun <T : Any> putServiceAs(t: T, clazz: KClass<out T>, name: String? = null)
    fun <T : Any> putServiceAs(t: T, clazz: Class<out T>) = putServiceAs(t, clazz.kotlin)
    fun putService(t: Any, name: String?): Unit = putServiceAs(t, t::class, name)
    fun putService(t: Any): Unit = putServiceAs(t, t::class)
}

fun <T : Any> BContext.tryGetService(kClass: KClass<T>): ServiceResult<T> = serviceContainer.tryGetService(kClass)
inline fun <reified T : Any> BContext.tryGetService(): ServiceResult<T> = serviceContainer.tryGetService<T>()
inline fun <reified T : Any> ServiceContainer.tryGetService(): ServiceResult<T> = tryGetService(T::class)

fun <T : Any> BContext.canCreateService(kClass: KClass<T>): ServiceError? = serviceContainer.canCreateService(kClass)
inline fun <reified T : Any> BContext.canCreateService(): ServiceError? = serviceContainer.canCreateService(T::class)
inline fun <reified T : Any> ServiceContainer.canCreateService(): ServiceError? = canCreateService(T::class)

fun <T : Any> BContext.getService(kClass: KClass<T>): T = serviceContainer.getService(kClass)
inline fun <reified T : Any> BContext.getService(): T = serviceContainer.getService<T>()
inline fun <reified T : Any> ServiceContainer.getService(): T = getService(T::class)

fun <T : Any> BContext.getServiceOrNull(kClass: KClass<T>): T? = serviceContainer.getServiceOrNull(kClass)
inline fun <reified T : Any> BContext.getServiceOrNull(): T? = serviceContainer.getServiceOrNull<T>()
inline fun <reified T : Any> ServiceContainer.getServiceOrNull(): T? = getServiceOrNull(T::class)

fun <T : Any> BContext.putServiceAs(t: T, kClass: KClass<T>) = serviceContainer.putServiceAs(t, kClass)
inline fun <reified T : Any> BContext.putServiceAs(t: T) = serviceContainer.putServiceAs<T>(t)
inline fun <reified T : Any> ServiceContainer.putServiceAs(t: T) = putServiceAs(t, T::class)

/**
 * Filters out interfaced services of that type if they are already being inspected.
 *
 * This allows you to check other implementations of your own interfaced service, without having a circular dependency.
 */
inline fun <reified T : Any> BContext.getInterfacedServiceTypes() = serviceContainer.getInterfacedServiceTypes<T>()
/**
 * Filters out interfaced services of that type if they are already being inspected.
 *
 * This allows you to check other implementations of your own interfaced service, without having a circular dependency.
 */
inline fun <reified T : Any> ServiceContainer.getInterfacedServiceTypes() = getInterfacedServiceTypes(T::class)

/**
 * Filters out interfaced services of that type if they are already being inspected.
 *
 * This allows you to check other implementations of your own interfaced service, without having a circular dependency.
 */
inline fun <reified T : Any> BContext.getInterfacedServices() = serviceContainer.getInterfacedServices<T>()
/**
 * Filters out interfaced services of that type if they are already being inspected.
 *
 * This allows you to check other implementations of your own interfaced service, without having a circular dependency.
 */
inline fun <reified T : Any> ServiceContainer.getInterfacedServices() = getInterfacedServices(T::class)

inline fun <reified R : Any> ServiceContainer.lazy(): Lazy<R> = lazy { this.getService(R::class) }

fun <R : Any> ServiceContainer.lazy(clazz: KClass<R>): Lazy<R> = lazy { this.getService(clazz) }

inline fun <reified R : Any> ServiceContainer.lazy(name: String): Lazy<R> = lazy { this.getService(name, R::class) }

inline operator fun <reified T : Any> ServiceContainer.getValue(thisRef: Any?, prop: KProperty<*>): T {
    return getService(T::class.java)
}