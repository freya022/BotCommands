package com.freya02.botcommands.api.localization.arguments

import java.text.MessageFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal class MessageFormatArgument internal constructor(
    override val argumentName: String,
    formatter: String,
    locale: Locale
) : FormattableArgument {
    private val lock = ReentrantLock()
    private val formatter = MessageFormat(formatter, locale)

    override fun format(obj: Any): String = lock.withLock { formatter.format(arrayOf(obj)) }
}