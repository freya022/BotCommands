package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.mixins.INamedCommandInfo
import com.freya02.botcommands.internal.commands.application.mixins.INamedCommandInfo.Companion.computePath
import net.dv8tion.jda.api.Permission
import java.util.*

abstract class AbstractCommandInfo internal constructor(
    context: BContextImpl,
    builder: CommandBuilder
) : Cooldownable(context, builder.cooldownStrategy), INamedCommandInfo {
    final override val name: String
    final override val path: CommandPath by lazy { computePath() }

    val userPermissions: EnumSet<Permission>
    val botPermissions: EnumSet<Permission>
    val nsfwStrategy: NSFWStrategy?

    init {
        name = builder.name
        nsfwStrategy = builder.nsfwStrategy
        userPermissions = builder.userPermissions
        botPermissions = builder.botPermissions
    }

    override fun toString(): String {
        return "${this::class.simpleName}: ${path.fullPath}"
    }
}