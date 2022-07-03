package com.freya02.botcommands.internal.application.context.user

import com.freya02.botcommands.api.application.builder.UserCommandBuilder
import com.freya02.botcommands.api.application.context.user.GlobalUserEvent
import com.freya02.botcommands.api.application.context.user.GuildUserEvent
import com.freya02.botcommands.api.parameters.UserContextParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.MethodParameters
import com.freya02.botcommands.internal.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.requireFirstParam
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.valueParameters

class UserCommandInfo internal constructor(
    context: BContextImpl,
    builder: UserCommandBuilder
) : ApplicationCommandInfo(context, builder) {
    override val parameters: MethodParameters

    init {
        requireFirstParam(method.valueParameters, GlobalUserEvent::class)

        parameters = MethodParameters.of<UserContextParameterResolver>(
            context,
            method,
            builder.optionBuilders
        ) { kParameter, paramName, resolver ->
            UserContextCommandParameter(kParameter, resolver)
        }
    }

    @Throws(Exception::class)
    suspend fun execute(
        context: BContextImpl,
        event: UserContextInteractionEvent
    ): Boolean {
        val objects: MutableList<Any?> = ArrayList(parameters.size + 1)
        objects +=
            if (isGuildOnly) GuildUserEvent(method, context, event) else GlobalUserEvent(method, context, event)

        for (parameter in parameters) {
            objects += when (parameter.methodParameterType) {
                MethodParameterType.COMMAND -> {
                    parameter as UserContextCommandParameter

                    parameter.resolver.resolve(context, this, event)
                }
                MethodParameterType.CUSTOM -> {
                    parameter as CustomMethodParameter

                    parameter.resolver.resolve(context, this, event)
                }
                else -> TODO()
            }
        }

        applyCooldown(event)

        method.callSuspend(*objects.toTypedArray())

        return true
    }
}