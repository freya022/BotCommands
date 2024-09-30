package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.LazyService
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceError
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

private sealed class AbstractLazyService<out T : Any> : LazyService<T> {

    private val lock = ReentrantLock()
    private lateinit var service: T

    final override fun canCreateService(): Boolean = getServiceError() == null

    final override fun getServiceError(): ServiceError? = lock.withLock {
        retrieveServiceError()
    }

    abstract fun retrieveServiceError(): ServiceError?

    final override val value: T
        get() {
            if (::service.isInitialized)
                return service

            return lock.withLock {
                if (::service.isInitialized.not())
                    service = retrieveService()

                service
            }
        }

    abstract fun retrieveService(): T

    final override fun isInitialized(): Boolean = ::service.isInitialized

    final override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."
}

private class ImplicitNamedLazyServiceImpl<out T : Any>(
    private val serviceContainer: ServiceContainer,
    private val type: KClass<T>,
    private val name: String?, // Null if the parameter has no name :(
) : AbstractLazyService<T>() {

    override fun retrieveServiceError(): ServiceError? {
        // Try getting an implicitly named service, if it has no error, all good
        if (name != null) {
            val namedError = serviceContainer.canCreateService(name, type)
            if (namedError == null) {
                return null
            }
        }

        // Type only
        return serviceContainer.canCreateService(type)
    }

    override fun retrieveService(): T {
        // Try to get the (implicitly) named service
        if (name != null) {
            val namedServiceResult = serviceContainer.tryGetService(name, type)
            if (namedServiceResult.service != null) {
                return namedServiceResult.service
            }
        }

        // Get the typed service, throw its exception otherwise, without the implicitly named one
        return serviceContainer.tryGetService(type).getOrThrow()
    }
}

private class LazyServiceImpl<out T : Any>(
    private val serviceContainer: ServiceContainer,
    private val type: KClass<T>,
    private val name: String?,
) : AbstractLazyService<T>() {

    override fun retrieveServiceError(): ServiceError? {
        return if (name != null) {
            serviceContainer.canCreateService(name, type)
        } else {
            serviceContainer.canCreateService(type)
        }
    }

    override fun retrieveService(): T {
        return if (name != null) {
            serviceContainer.getService(name, type)
        } else {
            serviceContainer.getService(type)
        }
    }
}

private class FallbackLazyServiceImpl<out T : Any>(
    private val serviceContainer: ServiceContainer,
    private val type: KClass<T>,
    private val name: String?,
    fallbackSupplier: () -> T,
) : AbstractLazyService<T>() {
    private var fallbackSupplier: (() -> T)? = fallbackSupplier

    override fun retrieveServiceError(): ServiceError? = null

    override fun retrieveService(): T {
        val service = if (name != null) {
            serviceContainer.getServiceOrNull(name, type)
        } else {
            serviceContainer.getServiceOrNull(type)
        } ?: fallbackSupplier!!()

        fallbackSupplier = null

        return service
    }
}

internal fun <T : Any> ServiceContainer.implicitlyNamedLazyService(clazz: KClass<T>, name: String?): LazyService<T> =
    ImplicitNamedLazyServiceImpl(this, clazz, name)

@PublishedApi
internal fun <T : Any> ServiceContainer.lazyService(clazz: KClass<T>, name: String?): LazyService<T> =
    LazyServiceImpl(this, clazz, name)

@PublishedApi
internal fun <T : Any, R : T> ServiceContainer.lazyServiceOrElse(clazz: KClass<T>, name: String?, block: () -> R): LazyService<T> =
    FallbackLazyServiceImpl(this, clazz, name, block)