package com.freya02.botcommands.internal.localization

internal class JavaFormattableString internal constructor(
    override val formatterName: String,
    private val formatter: String?
) : FormattableString {
    override fun format(obj: Any?): String {
        return formatter?.format(obj) ?: obj.toString()
    }
}