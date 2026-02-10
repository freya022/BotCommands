package dev.freya02.botcommands.method.accessors.internal

import java.lang.reflect.Executable
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaConstructor
import kotlin.reflect.jvm.javaMethod

class CachingMethodAccessorFactory(private val delegate: MethodAccessorFactory) : MethodAccessorFactory {

    private val cache = WeakHashMap<Executable, MethodAccessor<*>>()
    private val lock = ReentrantLock()

    override fun <R> create(
        instance: Any?,
        function: KFunction<R>,
    ): MethodAccessor<R> {
        return lock.withLock {
            val executable = function.javaMethod ?: function.javaConstructor

            @Suppress("UNCHECKED_CAST")
            val accessor = cache[executable] as MethodAccessor<R>?
            if (accessor != null) {
                accessor
            } else {
                val accessor = delegate.create(instance, function)
                cache[executable] = accessor
                accessor
            }
        }
    }
}
