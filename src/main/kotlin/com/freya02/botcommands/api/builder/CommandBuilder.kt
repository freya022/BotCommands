package com.freya02.botcommands.api.builder

import com.freya02.botcommands.api.commands.CooldownScope
import com.freya02.botcommands.api.commands.application.builder.OptionBuilder
import com.freya02.botcommands.internal.commands.CooldownStrategy
import com.freya02.botcommands.internal.enumSetOf
import net.dv8tion.jda.api.Permission
import java.util.*
import java.util.concurrent.TimeUnit

abstract class CommandBuilder internal constructor(val name: String) {
    var userPermissions: EnumSet<Permission> = enumSetOf()
    var botPermissions: EnumSet<Permission> = enumSetOf()

    @get:JvmSynthetic
    internal var cooldownStrategy: CooldownStrategy = CooldownStrategy(0, TimeUnit.SECONDS, CooldownScope.USER)
        private set

    @get:JvmSynthetic
    internal val optionBuilders: MutableMap<String, OptionBuilder> = mutableMapOf()

    fun cooldown(block: CooldownStrategyBuilder.() -> Unit) {
        cooldownStrategy = CooldownStrategyBuilder().apply(block).build()
    }
}
