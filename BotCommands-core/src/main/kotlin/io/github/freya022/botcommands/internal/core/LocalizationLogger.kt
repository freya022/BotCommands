package io.github.freya022.botcommands.internal.core

import io.github.freya022.botcommands.api.core.utils.loggerOf
import io.github.freya022.botcommands.internal.utils.stackWalker
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.slf4j.toKLogger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class LocalizationLogger internal constructor(private val logger: KLogger) {
    private val set: MutableSet<String> = hashSetOf()

    fun warn(vararg keyComponents: Any, message: () -> Any?) {
        if (set.add(keyComponents.joinToString("/"))) {
            logger.warn(message)
        }
    }

    companion object {
        private val map: MutableMap<String, LocalizationLogger> = hashMapOf()

        internal fun clearAll() {
            map.clear()
        }

        internal fun current() = get(stackWalker.callerClass)

        internal operator fun get(clazz: Class<*>) = LoggerFactory.getLogger(clazz).toKLogger().toSingleLogger()

        internal operator fun get(clazz: KClass<*>) = get(clazz.java)

        inline fun <reified T : Any> of() = KotlinLogging.loggerOf<T>().toSingleLogger()

        @PublishedApi
        internal fun KLogger.toSingleLogger() = map.computeIfAbsent(this.name) { LocalizationLogger(this) }
    }
}
