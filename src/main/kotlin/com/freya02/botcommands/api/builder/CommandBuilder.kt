package com.freya02.botcommands.api.builder

import com.freya02.botcommands.api.CooldownScope
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.internal.CooldownStrategy
import com.freya02.botcommands.internal.NSFWState
import com.freya02.botcommands.internal.enumSetOf
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.Permission
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction

abstract class CommandBuilder internal constructor(val instance: Any, val path: CommandPath) {
    var commandId: String? = null

    var userPermissions: EnumSet<Permission> = enumSetOf()
    var botPermissions: EnumSet<Permission> = enumSetOf()

    var cooldownStrategy: CooldownStrategy = CooldownStrategy(0, TimeUnit.SECONDS, CooldownScope.USER)

    var nsfwState: NSFWState? = null //TODO make DSL
        private set
//    val commandId: String? = null //TODO unneeded, implement via per-guild command construction

    internal abstract val optionBuilders: Map<String, OptionBuilder>

    lateinit var function: KFunction<*>

    internal fun isFunctionInitialized() = ::function.isInitialized

    internal inline fun <reified T : OptionBuilder> findOption(name: String): T {
        return optionBuilders[name] as? T ?: throwUser(
            "Option '$name' was not found in the command declaration, declared options: ${
                optionBuilders.map { it.value.name }.joinToString(separator = "', '", prefix = "'", postfix = "'")
            }"
        )
    }
}
