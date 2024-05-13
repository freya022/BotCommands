package io.github.freya022.botcommands.api.core.service

import io.github.freya022.botcommands.internal.core.service.LazyServiceImpl
import kotlin.reflect.KClass

interface LazyService<out T : Any> : Lazy<T> {
    fun canCreateService(): Boolean

    fun getServiceError(): ServiceError?
}

inline fun <reified T : Any> ServiceContainer.lazyService(name: String? = null): LazyService<T> =
    LazyServiceImpl(this, T::class, name)
fun <T : Any> ServiceContainer.lazyService(clazz: KClass<T>, name: String? = null): LazyService<T> =
    LazyServiceImpl(this, clazz, name)