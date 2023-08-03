package com.freya02.botcommands.internal.localization

import java.text.MessageFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class MessageFormatString internal constructor(
    override val formatterName: String,
    formatter: String,
    locale: Locale
) : FormattableString {
    private val lock = ReentrantLock()
    private val formatter = MessageFormat(formatter, locale)

    override fun format(obj: Any): String {
        return lock.withLock { formatter.format(arrayOf(obj)) }
    }
}