package com.freya02.botcommands.internal.localization

internal class RawString internal constructor(private val string: String) : LocalizableString {
    fun get() = string
}