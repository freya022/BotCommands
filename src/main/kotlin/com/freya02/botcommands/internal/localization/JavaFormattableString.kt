package com.freya02.botcommands.internal.localization

class JavaFormattableString(override val formatterName: String, private val formatter: String?) : FormattableString {
    override fun format(obj: Any?): String {
        return formatter?.format(obj) ?: obj.toString()
    }
}