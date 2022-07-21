package com.freya02.botcommands.api.builder

import com.freya02.botcommands.api.CooldownScope
import com.freya02.botcommands.internal.CooldownStrategy
import java.util.concurrent.TimeUnit

class CooldownStrategyBuilder internal constructor() {
    var cooldown: Long = 0
    var unit: TimeUnit = TimeUnit.SECONDS
    var scope: CooldownScope = CooldownScope.USER

    internal fun build(): CooldownStrategy {
        return CooldownStrategy(cooldown, unit, scope)
    }
}
