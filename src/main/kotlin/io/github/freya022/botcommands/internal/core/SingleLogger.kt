package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.internal.utils.stackWalker
import kotlin.reflect.KClass

internal class SingleLogger {
    private val set: MutableSet<String> = hashSetOf()

    fun tryLog(vararg keyComponents: Any): Boolean = set.add(keyComponents.joinToString("/") { it.toString() })

    @JvmSynthetic
    fun tryLog(vararg keyComponents: Any, block: () -> Unit) {
        if (tryLog(*keyComponents)) {
            block()
        }
    }

    fun clear() {
        set.clear()
    }

    companion object {
        private val map: MutableMap<Class<*>, SingleLogger> = hashMapOf()

        @JvmStatic
        fun current() = get(stackWalker.callerClass)

        @JvmStatic
        operator fun get(clazz: Class<*>) = map.computeIfAbsent(clazz) { SingleLogger() }

        operator fun get(clazz: KClass<*>) = get(clazz.java)
    }
}