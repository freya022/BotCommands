package com.freya02.botcommands.internal.localization

import com.freya02.botcommands.api.localization.arguments.FormattableArgument

internal class SimpleArgument internal constructor(override val formatterName: String) : FormattableArgument {
    override fun format(obj: Any): String = obj.toString()
}