package io.github.freya022.botcommands.method.accessors.internal

import java.util.*
import kotlin.reflect.KFunction

interface MethodAccessorFactory {

    // TODO is there a better way to prioritize the classfile implementation?
    val priority: Int

    fun create(instance: Any, function: KFunction<*>): MethodAccessor

    companion object {

        fun findAll(): List<MethodAccessorFactory> {
            return ServiceLoader.load(MethodAccessorFactory::class.java).toList()
        }
    }
}
