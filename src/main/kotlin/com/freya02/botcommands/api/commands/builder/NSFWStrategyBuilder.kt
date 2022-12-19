package com.freya02.botcommands.api.commands.builder

import com.freya02.botcommands.internal.commands.NSFWStrategy

class NSFWStrategyBuilder internal constructor() {
    var allowInGuild: Boolean = false
    var allowInDMs: Boolean = false

    internal fun build(): NSFWStrategy {
        return NSFWStrategy(allowInGuild, allowInDMs)
    }
}
