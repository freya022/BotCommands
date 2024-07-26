package io.github.freya022.botcommands.internal.application.diff

import io.github.freya022.botcommands.api.core.BContext
import io.github.oshai.kotlinlogging.KotlinLogging

internal sealed class DiffLogger {
    internal abstract fun copyWithKey(key: String): DiffLogger

    internal abstract fun log(formatStr: String, vararg objects: Any?)

    internal abstract fun printLogs()

    internal companion object {
        internal val logger = KotlinLogging.logger {  }

        internal fun getLogger(context: BContext): DiffLogger = when {
            logger.isTraceEnabled() && context.debugConfig.enableApplicationDiffsLogs -> DiffLoggerImpl(emptyList())
            else -> DiffLoggerNoop
        }
    }
}

internal data object DiffLoggerNoop : DiffLogger() {
    override fun copyWithKey(key: String): DiffLogger = DiffLoggerNoop

    override fun log(formatStr: String, vararg objects: Any?) {}

    override fun printLogs() {}
}

internal class DiffLoggerImpl(private val keys: List<String>, private val logItems: MutableList<String> = arrayListOf()) : DiffLogger() {
    override fun copyWithKey(key: String): DiffLogger = DiffLoggerImpl(keys + key, logItems)

    override fun log(formatStr: String, vararg objects: Any?) {
        logItems += "\t".repeat(keys.size - 1) + "${keys.joinToString(".")} - " + String.format(formatStr, *objects)
    }

    override fun printLogs() = logItems.forEach { logger.trace { it } }
}

internal fun DiffLogger.logSame(formatStr: String, vararg objects: Any?): Boolean {
    log(formatStr, *objects)
    return true
}

internal fun DiffLogger.logDifferent(formatStr: String, vararg objects: Any?): Boolean {
    log(formatStr, *objects)
    return false
}

internal inline fun DiffLogger.withKey(key: String, block: DiffLogger.() -> Unit) {
    block(copyWithKey(key))
}

internal inline fun <R> DiffLogger.ignoreLogs(block: DiffLogger.() -> R): R = block(DiffLoggerNoop)