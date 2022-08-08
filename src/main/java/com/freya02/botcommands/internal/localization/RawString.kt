package com.freya02.botcommands.internal.localization

class RawString(private val string: String) : LocalizableString {
    fun get() = string
}