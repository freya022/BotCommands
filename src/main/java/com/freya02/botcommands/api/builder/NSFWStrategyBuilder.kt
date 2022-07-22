package com.freya02.botcommands.api.builder

import com.freya02.botcommands.internal.NSFWStrategy

class NSFWStrategyBuilder internal constructor() {
    var allowInGuild: Boolean = false
    var allowInDMs: Boolean = false

    internal fun build(): NSFWStrategy {
        return NSFWStrategy(allowInGuild, allowInDMs)
    }
}
