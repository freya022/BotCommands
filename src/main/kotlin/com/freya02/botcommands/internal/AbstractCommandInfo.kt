package com.freya02.botcommands.internal

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.builder.CommandBuilder
import net.dv8tion.jda.api.Permission
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters

abstract class AbstractCommandInfo internal constructor(
    context: BContextImpl,
    builder: CommandBuilder
) : Cooldownable(builder.cooldownStrategy), ExecutableInteractionInfo {
    val path: CommandPath
    val isOwnerRequired: Boolean
    val userPermissions: EnumSet<Permission>
    val botPermissions: EnumSet<Permission>
    val nsfwStrategy: NSFWStrategy?
    val commandId: String?

    final override val instance: Any
    final override val method: KFunction<*>

    init {
        instance = context.serviceContainer.getFunctionService(builder.function)

        path = builder.path
        method = builder.function
        isOwnerRequired = false //TODO remove ownerRequired from application, move to Text
        commandId = builder.commandId
        nsfwStrategy = builder.nsfwStrategy
        userPermissions = builder.userPermissions
        botPermissions = builder.botPermissions

        requireUser(builder.optionBuilders.size == method.valueParameters.size - 1) {  //-1 for the event
            "Function must have the same number of options declared as on the method"
        }
    }
}