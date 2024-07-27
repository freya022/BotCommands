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