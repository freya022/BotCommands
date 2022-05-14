package com.freya02.botcommands.internal

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.builder.CommandBuilder
import com.freya02.botcommands.internal.runner.MethodRunner
import net.dv8tion.jda.api.Permission
import java.util.*
import java.util.function.Consumer
import kotlin.reflect.KFunction

/**
 * @param <T> Command instance type
</T> */
abstract class AbstractCommandInfo protected constructor(
    context: BContext,
    builder: CommandBuilder
) : Cooldownable(builder.cooldownStrategy), ExecutableInteractionInfo {
    private val instance: Any

    val path: CommandPath

    val isOwnerRequired: Boolean

    protected val commandMethod: KFunction<*>
    val userPermissions: EnumSet<Permission>
    val botPermissions: EnumSet<Permission>
    val nsfwState: NSFWState?
    val commandId: String?
    private val methodRunner: MethodRunner

    init {
        instance = builder.instance
        path = builder.path
        commandMethod = builder.function
        isOwnerRequired = false //TODO remove ownerRequired from application, move to Text
        commandId = builder.commandId
        nsfwState = builder.nsfwState
        userPermissions = builder.userPermissions
        botPermissions = builder.botPermissions

        methodRunner = object : MethodRunner {
            //TODO replace
            @Suppress("UNCHECKED_CAST")
            override fun <R> invoke(
                args: Array<Any>,
                throwableConsumer: Consumer<Throwable>,
                successCallback: ConsumerEx<R>
            ) {
                try {
                    val call = commandMethod.call(*args)
                    successCallback.accept(call as R)
                } catch (e: Throwable) {
                    throwableConsumer.accept(e)
                }
            }
        }
    }

    override fun getMethod(): KFunction<*> = commandMethod

    override fun getMethodRunner(): MethodRunner = methodRunner

    override fun getInstance(): Any = instance
}