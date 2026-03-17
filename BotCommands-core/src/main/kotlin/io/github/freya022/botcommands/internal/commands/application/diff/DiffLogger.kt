package io.github.freya022.botcommands.internal.commands.application.diff

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

internal sealed class DiffLogger {
    internal abstract fun log(message: () -> Any?)

    internal abstract fun printLogs()

    internal companion object {
        internal fun <R> withLogger(title: String, block: DiffLogger.() -> R): R {
            return when {
                logger.isTraceEnabled() -> {
                    val diffLogger = DiffLoggerImpl(title)
                    val value = diffLogger.block()
                    diffLogger.printLogs()
                    value
                }
                else -> DiffLoggerNoop.block()
            }
        }
    }
}

internal data object DiffLoggerNoop : DiffLogger() {
    override fun log(message: () -> Any?) {}

    override fun printLogs() {}
}

internal class DiffLoggerImpl(private val title: String) : DiffLogger() {
    private val logItems: MutableList<() -> Any?> = arrayListOf()

    override fun log(message: () -> Any?) {
        logItems += message
    }

    override fun printLogs() {
        if (logItems.isEmpty()) return

        logger.trace {
            val diffs = logItems.joinToString("\n") { " - ${it()}" }
            "Command changes on $title:\n$diffs"
        }
    }
}
