package com.freya02.botcommands.api.localization.arguments

import java.text.MessageFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class MessageFormatArgument(
    override val argumentName: String,
    formatter: String,
    locale: Locale
) : FormattableArgument {
    private val lock = ReentrantLock()
    private val formatter = MessageFormat(formatter, locale)

    override fun format(obj: Any): String = lock.withLock { formatter.format(arrayOf(obj)) }

    override fun toString(): String {
        return "MessageFormatArgument(argumentName='$argumentName', formatter=${formatter.toPattern()})"
    }
}