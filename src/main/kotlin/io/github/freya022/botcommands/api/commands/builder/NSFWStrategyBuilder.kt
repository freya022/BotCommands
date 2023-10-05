package io.github.freya022.botcommands.api.commands.builder

import io.github.freya022.botcommands.internal.commands.CommandDSL
import io.github.freya022.botcommands.internal.commands.NSFWStrategy

@CommandDSL
class NSFWStrategyBuilder internal constructor() {
    var allowInGuild: Boolean = false
    var allowInDMs: Boolean = false

    internal fun build(): NSFWStrategy {
        return NSFWStrategy(allowInGuild, allowInDMs)
    }
}
