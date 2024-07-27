package io.github.freya022.botcommands.internal.application.diff

import io.github.freya022.botcommands.api.core.BContext
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

internal sealed class DiffLogger {
    internal abstract fun log(message: () -> Any?)

    internal abstract fun printLogs()

    internal companion object {
        internal fun <R> withLogger(context: BContext, block: DiffLogger.() -> R): R = when {
            logger.isTraceEnabled() && context.debugConfig.enableApplicationDiffsLogs -> {
                val diffLogger = DiffLoggerImpl()
                val value = diffLogger.block()
                diffLogger.printLogs()
                value
            }
            else -> DiffLoggerNoop.block()
        }
    }
}

internal data object DiffLoggerNoop : DiffLogger() {
    override fun log(message: () -> Any?) {}

    override fun printLogs() {}
}

internal class DiffLoggerImpl : DiffLogger() {
    private val logItems: MutableList<() -> Any?> = arrayListOf()

    override fun log(message: () -> Any?) {
        logItems += message
    }

    override fun printLogs() = logItems.forEach(logger::trace)
}

internal fun DiffLogger.logSame(indent: Int, message: () -> Any?): Boolean {
    log {
        "\t".repeat(indent) + message()
    }
    return true
}

internal fun DiffLogger.logDifferent(indent: Int, message: () -> Any?): Boolean {
    log {
        "\t".repeat(indent) + message()
    }
    return false
}

internal inline fun DiffLogger.withKey(key: String, block: DiffLogger.() -> Unit) {
    block(this)
}

internal inline fun <R> DiffLogger.ignoreLogs(block: DiffLogger.() -> R): R = block(DiffLoggerNoop)