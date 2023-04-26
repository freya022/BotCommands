package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.builder.CommandBuilder
import com.freya02.botcommands.internal.commands.mixins.INamedCommand
import com.freya02.botcommands.internal.commands.mixins.INamedCommand.Companion.computePath
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.Permission
import java.util.*

abstract class AbstractCommandInfo internal constructor(
    builder: CommandBuilder
) : Cooldownable, INamedCommand {
    final override val name: String
    final override val path: CommandPath by lazy { computePath() }

    override val cooldownStrategy: CooldownStrategy = builder.cooldownStrategy

    val userPermissions: EnumSet<Permission>
    val botPermissions: EnumSet<Permission>

    init {
        name = builder.name
        userPermissions = builder.userPermissions
        botPermissions = builder.botPermissions

        builder.optionAggregateBuilders.values.forEach { optionAggregateBuilder ->
            optionAggregateBuilder.optionBuilders.values.forEach { optionBuilder ->
                if (!optionBuilder.hasType()) {
                    throwUser("Option ${optionBuilder.declaredName} in option aggregate '${optionAggregateBuilder.declaredName}' should have a type set")
                }
            }
        }
    }

    override fun toString(): String {
        return "${this::class.simpleName}: ${path.fullPath}"
    }
}