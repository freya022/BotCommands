package com.freya02.botcommands.api.application.builder

import com.freya02.botcommands.internal.throwUser

abstract class OptionBuilder(val name: String)

internal inline fun <reified T : OptionBuilder> Map<String, OptionBuilder>.findOption(name: String): T {
    return this[name] as? T ?: throwUser(
        "Option '$name' was not found in the command declaration, declared options: ${
            this.keys.joinToString(separator = "', '", prefix = "'", postfix = "'")
        }"
    )
}
