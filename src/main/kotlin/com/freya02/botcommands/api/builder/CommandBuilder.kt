package com.freya02.botcommands.api.builder

import com.freya02.botcommands.api.CooldownScope
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.internal.CooldownStrategy
import com.freya02.botcommands.internal.NSFWStrategy
import com.freya02.botcommands.internal.enumSetOf
import net.dv8tion.jda.api.Permission
import java.util.*
import java.util.concurrent.TimeUnit

abstract class CommandBuilder internal constructor(val path: CommandPath) : BuilderFunctionHolder<Any>() {
    var commandId: String? = null

    var userPermissions: EnumSet<Permission> = enumSetOf()
    var botPermissions: EnumSet<Permission> = enumSetOf()

    internal var cooldownStrategy: CooldownStrategy = CooldownStrategy(0, TimeUnit.SECONDS, CooldownScope.USER)
        private set

    internal var nsfwStrategy: NSFWStrategy? = null //TODO make DSL
        private set
//    val commandId: String? = null //TODO unneeded, implement via per-guild command construction

    internal val optionBuilders: MutableMap<String, OptionBuilder> = mutableMapOf()

    fun cooldown(block: CooldownStrategyBuilder.() -> Unit) {
        cooldownStrategy = CooldownStrategyBuilder().apply(block).build()
    }

    fun nsfw(block: NSFWStrategyBuilder.() -> Unit) {
        nsfwStrategy = NSFWStrategyBuilder().apply(block).build()
    }
}
