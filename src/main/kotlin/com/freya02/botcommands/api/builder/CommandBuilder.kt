package com.freya02.botcommands.api.builder

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.internal.NSFWState
import com.freya02.botcommands.internal.enumSetOf
import net.dv8tion.jda.api.Permission
import java.util.*
import kotlin.reflect.KFunction

abstract class CommandBuilder internal constructor(val path: CommandPath) {
    var userPermissions: EnumSet<Permission> = enumSetOf()
    var botPermissions: EnumSet<Permission> = enumSetOf()

    protected val nsfwState: NSFWState? = null //TODO make DSL
//    val commandId: String? = null //TODO unneeded, implement via per-guild command construction

    lateinit var function: KFunction<*>
}
