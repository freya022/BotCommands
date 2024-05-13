package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.service.LazyService
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.ServiceError
import okio.withLock
import java.util.concurrent.locks.ReentrantLock
import kotlin.reflect.KClass

@PublishedApi
internal class ImplicitNamedLazyServiceImpl<out T : Any>(
    private val serviceContainer: ServiceContainer,
    private val type: KClass<T>,
    private val name: String?, // Null if the parameter has no name :(
) : LazyService<T> {
    private val lock = ReentrantLock()
    private lateinit var service: T

    override fun canCreateService(): Boolean = getServiceError() == null

    override fun getServiceError(): ServiceError? = lock.withLock {
        // If the named service has no error, then return no error
        if (name != null && serviceContainer.canCreateService(name, type) == null)
            return@withLock null
        else
            serviceContainer.canCreateService(type)
    }

    override val value: T
        get() = lock.withLock {
            if (::service.isInitialized.not()) {
                // Try to get the (implicitly) named service
                if (name != null) {
                    val namedServiceResult = serviceContainer.tryGetService(name, type)
                    if (namedServiceResult.service != null) {
                        service = namedServiceResult.service
                        return@withLock service
                    }
                }

                // Get the typed service, throw its exception otherwise, without the implicitly named one
                val serviceResult = serviceContainer.tryGetService(type)
                service = serviceResult.getOrThrow()
            }

            service
        }

    override fun isInitialized(): Boolean = ::service.isInitialized

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."
}

@PublishedApi
internal class LazyServiceImpl<out T : Any>(
    private val serviceContainer: ServiceContainer,
    private val type: KClass<T>,
    private val name: String?,
) : LazyService<T> {
    private val lock = ReentrantLock()
    private lateinit var service: T

    override fun canCreateService(): Boolean = getServiceError() == null

    override fun getServiceError(): ServiceError? = lock.withLock {
        if (name != null) {
            serviceContainer.canCreateService(name, type)
        } else {
            serviceContainer.canCreateService(type)
        }
    }

    override val value: T
        get() = lock.withLock {
            if (::service.isInitialized.not()) {
                service = if (name != null) {
                    serviceContainer.getService(name, type)
                } else {
                    serviceContainer.getService(type)
                }
            }

            service
        }

    override fun isInitialized(): Boolean = ::service.isInitialized

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."
}