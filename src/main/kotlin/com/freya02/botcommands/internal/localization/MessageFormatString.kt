package com.freya02.botcommands.internal.localization

import java.text.MessageFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class MessageFormatString(override val formatterName: String, formatter: String?, locale: Locale) : FormattableString {
    private val lock = ReentrantLock()
    private val formatter: MessageFormat?

    init {
        this.formatter = formatter?.let { MessageFormat(it, locale) }
    }

    override fun format(obj: Any?): String {
        if (formatter == null) return obj.toString()
        lock.withLock { return formatter.format(arrayOf(obj)) }
    }
}