package com.freya02.botcommands.internal

import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.internal.runner.MethodRunner
import net.dv8tion.jda.api.Permission
import java.util.*
import java.util.function.Consumer
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
    val nsfwState: NSFWState?
    val commandId: String?

    final override val instance: Any
    final override val methodRunner: MethodRunner
    final override val method: KFunction<*>

    init {
        instance = context.serviceContainer.getFunctionService(builder.function)

        path = builder.path
        method = builder.function
        isOwnerRequired = false //TODO remove ownerRequired from application, move to Text
        commandId = builder.commandId
        nsfwState = builder.nsfwState
        userPermissions = builder.userPermissions
        botPermissions = builder.botPermissions

        requireUser(builder.optionBuilders.size == method.valueParameters.size - 1) {  //-1 for the event
            "Function must have the same number of options declared as on the method"
        }

        methodRunner = object : MethodRunner {
            //TODO replace
            @Suppress("UNCHECKED_CAST")
            override fun <R> invoke(
                args: Array<Any>,
                throwableConsumer: Consumer<Throwable>,
                successCallback: ConsumerEx<R>
            ) {
                try {
                    val call = method.call(*args)
                    successCallback.accept(call as R)
                } catch (e: Throwable) {
                    throwableConsumer.accept(e)
                }
            }
        }
    }
}