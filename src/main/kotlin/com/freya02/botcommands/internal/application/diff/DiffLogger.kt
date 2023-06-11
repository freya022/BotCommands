package com.freya02.botcommands.internal.application.diff

import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.application.diff.DiffLogger.Companion.logger
import mu.KotlinLogging

internal interface DiffLogger {
    fun trace(indent: Int, formatStr: String, vararg objects: Any?)

    fun printLogs()

    companion object {
        internal val logger = KotlinLogging.logger {  }

        fun getLogger(context: BContextImpl): DiffLogger = when {
            !logger.isTraceEnabled -> DiffLoggerNoop()
            context.debugConfig.enableApplicationDiffsLogs -> DiffLoggerImpl()
            else -> DiffLoggerNoop()
        }
    }
}

internal class DiffLoggerNoop : DiffLogger {
    override fun trace(indent: Int, formatStr: String, vararg objects: Any?) {}
    override fun printLogs() {}
}

internal class DiffLoggerImpl : DiffLogger {
    private val logItems: MutableList<String> = arrayListOf()

    override fun trace(indent: Int, formatStr: String, vararg objects: Any?) {
        logItems += "\t".repeat(indent) + String.format(formatStr, *objects)
    }

    override fun printLogs() = logItems.forEach { logger.trace(it) }
}