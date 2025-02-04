package io.github.freya022.botcommands.api.core.service

interface LazyService<out T : Any> : Lazy<T> {
    fun canCreateService(): Boolean

    fun getServiceError(): ServiceError?
}
