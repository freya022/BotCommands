package io.github.freya022.botcommands.internal.core.service

import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.internal.utils.throwArgument
import okio.withLock
import java.util.concurrent.locks.ReentrantLock
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KVisibility

internal object Singletons {
    private val lock = ReentrantLock()
    private val singletonCache = hashMapOf<KClass<*>, Any>()

    @Suppress("UNCHECKED_CAST")
    internal operator fun <T : Any> get(clazz: KClass<T>): T {
        return lock.withLock {
            singletonCache.computeIfAbsent(clazz, ::createInstance) as T
        }
    }

    private fun createInstance(clazz: KClass<*>): Any {
        val instance = clazz.objectInstance
        if (instance != null)
            return instance

        val constructor = clazz.constructors.singleOrNull { it.parameters.all(KParameter::isOptional) }
            ?: throwArgument("Class ${clazz.shortQualifiedName} must either be an object, or have a no-arg constructor (or have only default parameters)")
        check(constructor.visibility == KVisibility.PUBLIC || constructor.visibility == KVisibility.INTERNAL) {
            "Constructor of ${clazz.shortQualifiedName} must be effectively public (internal is allowed)"
        }

        return constructor.callBy(mapOf())
    }
}