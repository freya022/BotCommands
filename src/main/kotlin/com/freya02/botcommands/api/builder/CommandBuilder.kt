package com.freya02.botcommands.api.builder

import com.freya02.botcommands.api.CooldownScope
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.internal.CooldownStrategy
import com.freya02.botcommands.internal.NSFWState
import com.freya02.botcommands.internal.enumSetOf
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.reflectReference
import net.dv8tion.jda.api.Permission
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction

private object Dummy {
    fun dummy(): Nothing = throwInternal("A command with no function set has been used")
}

private val NO_FUNCTION = Dummy::dummy

abstract class CommandBuilder internal constructor(val path: CommandPath) {
    var commandId: String? = null

    var userPermissions: EnumSet<Permission> = enumSetOf()
    var botPermissions: EnumSet<Permission> = enumSetOf()

    var cooldownStrategy: CooldownStrategy = CooldownStrategy(0, TimeUnit.SECONDS, CooldownScope.USER)

    var nsfwState: NSFWState? = null //TODO make DSL
        private set
//    val commandId: String? = null //TODO unneeded, implement via per-guild command construction

    internal abstract val optionBuilders: Map<String, OptionBuilder>

    var function: KFunction<*> = NO_FUNCTION
        set(value) {
            field = value.reflectReference()
        }

    internal fun isFunctionInitialized() = function !== NO_FUNCTION
}
