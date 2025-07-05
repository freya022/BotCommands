package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.internal.utils.stackWalker
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.slf4j.toKLogger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

internal class SingleLogger internal constructor(private val logger: KLogger) {
    private val set: MutableSet<String> = hashSetOf()

    internal fun warn(vararg keyComponents: Any, message: () -> Any?) {
        if (set.add(keyComponents.joinToString("/"))) {
            logger.warn(message)
        }
    }

    internal fun clear() {
        set.clear()
    }

    internal companion object {
        private val map: MutableMap<String, SingleLogger> = hashMapOf()

        internal fun current() = get(stackWalker.callerClass)

        internal operator fun get(clazz: Class<*>) = LoggerFactory.getLogger(clazz).toKLogger().toSingleLogger()

        internal operator fun get(clazz: KClass<*>) = get(clazz.java)

        internal inline fun <reified T : Any> of() = KotlinLogging.loggerOf<T>().toSingleLogger()

        internal fun KLogger.toSingleLogger() = map.computeIfAbsent(this.name) { SingleLogger(this) }
    }
}