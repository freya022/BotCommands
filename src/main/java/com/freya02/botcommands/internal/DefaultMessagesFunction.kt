package com.freya02.botcommands.internal

import com.freya02.botcommands.api.DefaultMessages
import java.util.*
import java.util.function.Function

class DefaultMessagesFunction : Function<Locale, DefaultMessages> {
    private val localeDefaultMessagesMap: MutableMap<Locale, DefaultMessages> = HashMap()

    override fun apply(locale: Locale): DefaultMessages {
        return localeDefaultMessagesMap.computeIfAbsent(locale, ::DefaultMessages)
    }
}