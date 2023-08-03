package com.freya02.botcommands.internal.localization

internal class JavaFormattableArgument internal constructor(
    override val formatterName: String,
    private val formatter: String?
) : FormattableArgument {
    override fun format(obj: Any): String {
        return formatter?.format(obj) ?: obj.toString()
    }
}