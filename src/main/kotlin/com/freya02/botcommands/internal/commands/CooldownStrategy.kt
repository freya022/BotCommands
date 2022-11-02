package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.commands.CooldownScope
import java.util.concurrent.TimeUnit

class CooldownStrategy(cooldown: Long, unit: TimeUnit, val scope: CooldownScope) {
    val cooldownMillis: Long = unit.toMillis(cooldown)
}