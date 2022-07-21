package com.freya02.botcommands.internal

import com.freya02.botcommands.api.CooldownScope
import java.util.concurrent.TimeUnit

class CooldownStrategy(cooldown: Long, unit: TimeUnit, val scope: CooldownScope) {
    val cooldownMillis: Long = unit.toMillis(cooldown)
}