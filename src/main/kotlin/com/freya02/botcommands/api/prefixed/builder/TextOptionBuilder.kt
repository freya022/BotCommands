package com.freya02.botcommands.api.prefixed.builder

import com.freya02.botcommands.api.application.builder.OptionBuilder

class TextOptionBuilder internal constructor(declaredName: String, optionName: String) : OptionBuilder(declaredName, optionName) {
    var example: String? = null
    var isId: Boolean = false
}
