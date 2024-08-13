package io.github.freya022.botcommands.api.core.service

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Logging
import io.github.freya022.botcommands.api.core.service.annotations.InjectedService
import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.internal.utils.currentFrame
import io.github.freya022.botcommands.internal.utils.toSignature
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@InjectedService
interface ServiceContainer {
    fun <T : Any> tryGetService(name: String, requiredType: KClass<T>): ServiceResult<T>
    fun <T : Any> tryGetService(name: String, requiredType: Class<T>): ServiceResult<T> = tryGetService(name, requiredType.kotlin)
    fun <T : Any> tryGetService(clazz: KClass<T>): ServiceResult<T>
    fun <T : Any> tryGetService(clazz: Class<T>): ServiceResult<T> = tryGetService(clazz.kotlin)

    fun canCreateService(name: String, requiredType: KClass<*>): ServiceError?
    fun canCreateService(name: String, requiredType: Class<*>): ServiceError? = canCreateService(name, requiredType.kotlin)

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

    fun getServiceNamesForAnnotation(annotationType: KClass<out Annotation>): Collection<String>
    fun getServiceNamesForAnnotation(annotationType: Class<out Annotation>): Collection<String> =
        getServiceNamesForAnnotation(annotationType.kotlin)

    fun <A : Annotation> findAnnotationOnService(name: String, annotationType: KClass<A>): A?
    fun <A : Annotation> findAnnotationOnService(name: String, annotationType: Class<A>): A? =
        findAnnotationOnService(name, annotationType.kotlin)

    fun <T : Any> peekServiceOrNull(name: String, requiredType: KClass<T>): T?
    fun <T : Any> peekServiceOrNull(name: String, requiredType: Class<T>): T? = peekServiceOrNull(name, requiredType.kotlin)
    fun <T : Any> peekServiceOrNull(clazz: KClass<T>): T?
    fun <T : Any> peekServiceOrNull(clazz: Class<T>): T? = peekServiceOrNull(clazz.kotlin)

    fun <T : Any> getInterfacedServiceTypes(clazz: KClass<T>): List<KClass<out T>>
    fun <T : Any> getInterfacedServiceTypes(clazz: Class<T>): List<Class<out T>> =
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

    fun <T : Any> putServiceAs(t: T, clazz: KClass<out T>, name: String)
    fun <T : Any> putServiceAs(t: T, clazz: KClass<out T>)
    fun <T : Any> putServiceAs(t: T, clazz: Class<out T>)
    fun putService(t: Any, name: String)
    fun putService(t: Any)
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

inline fun <reified A : Annotation> ServiceContainer.getServiceNamesForAnnotation(): Collection<String> =
    getServiceNamesForAnnotation(A::class)

inline fun <reified A : Annotation> ServiceContainer.findAnnotationOnService(name: String): A? =
    findAnnotationOnService(name, A::class)

fun <T : Any> BContext.getServiceOrNull(kClass: KClass<T>): T? = serviceContainer.getServiceOrNull(kClass)
inline fun <reified T : Any> BContext.getServiceOrNull(): T? = serviceContainer.getServiceOrNull<T>()
inline fun <reified T : Any> ServiceContainer.getServiceOrNull(): T? = getServiceOrNull(T::class)

inline fun <reified T : Any> ServiceContainer.putServiceAs(t: T, name: String) = putServiceAs(t, T::class, name)
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

//region Lazy service retrieval

// The U type parameter is used so the type produced by the block would not override the requested service's type.
inline fun <reified R : Any> ServiceContainer.lazy(): LazyService<R> = lazyService(R::class)
@Deprecated("Replace your Lazy#value call with ServiceContainer#getServiceOrNull")
inline fun <reified R : Any> ServiceContainer.lazyOrNull(): Lazy<R?> {
    KotlinLogging.loggerOf<ServiceContainer>().warn { "lazyOrNull has been deprecated and will be removed in a future version: ${currentFrame().toSignature()}" }
    return lazy { this.getServiceOrNull(R::class) }
}

inline fun <reified R : Any, U : R> ServiceContainer.lazyOrElse(crossinline block: () -> U): Lazy<R> = lazy { this.getServiceOrNull(R::class) ?: block() }

fun <R : Any> ServiceContainer.lazy(clazz: KClass<R>): LazyService<R> = lazyService(clazz)
@Deprecated("Replace your Lazy#value call with ServiceContainer#getServiceOrNull")
fun <R : Any> ServiceContainer.lazyOrNull(clazz: KClass<R>): Lazy<R?> {
    KotlinLogging.loggerOf<ServiceContainer>().warn { "lazyOrNull has been deprecated and will be removed in a future version: ${currentFrame().toSignature()}" }
    return lazy { this.getServiceOrNull(clazz) }
}
fun <R : Any, U : R> ServiceContainer.lazyOrElse(clazz: KClass<R>, block: () -> U): Lazy<R> = lazy { this.getServiceOrNull(clazz) ?: block() }

fun <R : Any> ServiceContainer.lazy(name: String, requiredType: KClass<R>): LazyService<R> = lazyService(requiredType, name)
@Deprecated("Replace your Lazy#value call with ServiceContainer#getServiceOrNull")
fun <R : Any> ServiceContainer.lazyOrNull(name: String, requiredType: KClass<R>): Lazy<R?> {
    KotlinLogging.loggerOf<ServiceContainer>().warn { "lazyOrNull has been deprecated and will be removed in a future version: ${currentFrame().toSignature()}" }
    return lazy { this.getServiceOrNull(name, requiredType) }
}
fun <R : Any, U : R> ServiceContainer.lazyOrElse(name: String, requiredType: KClass<R>, block: () -> U): Lazy<R> = lazy { this.getServiceOrNull(name, requiredType) ?: block() }

inline fun <reified R : Any> ServiceContainer.lazy(name: String): LazyService<R> = lazyService<R>(name)
@Deprecated("Replace your Lazy#value call with ServiceContainer#getServiceOrNull")
inline fun <reified R : Any> ServiceContainer.lazyOrNull(name: String): Lazy<R?> {
    Logging.currentLogger().warn { "lazyOrNull has been deprecated and will be removed in a future version: ${currentFrame().toSignature()}" }
    return lazy { this.getServiceOrNull(name, R::class) }
}
inline fun <reified R : Any, U : R> ServiceContainer.lazyOrElse(name: String, crossinline block: () -> U): Lazy<R> = lazy { this.getServiceOrNull(name, R::class) ?: block() }
//endregion

inline operator fun <reified T : Any> ServiceContainer.getValue(thisRef: Any?, prop: KProperty<*>): T {
    return getService(T::class.java)
}