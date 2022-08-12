package com.freya02.botcommands.internal.application.context.user

import com.freya02.botcommands.api.application.builder.UserCommandBuilder
import com.freya02.botcommands.api.application.builder.UserCommandOptionBuilder
import com.freya02.botcommands.api.application.context.user.GlobalUserEvent
import com.freya02.botcommands.api.application.context.user.GuildUserEvent
import com.freya02.botcommands.api.parameters.UserContextParameterResolver
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.application.slash.SlashUtils2.checkEventScope
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

class UserCommandInfo internal constructor(
    context: BContextImpl,
    builder: UserCommandBuilder
) : ApplicationCommandInfo(context, builder) {
    override val parameters: MethodParameters

    init {
        requireFirstParam(method.valueParameters, GlobalUserEvent::class)

        checkEventScope<GuildUserEvent>()

        @Suppress("RemoveExplicitTypeArguments") //Compiler bug
        parameters = MethodParameters2.transform<UserContextParameterResolver>(
            context,
            method,
            builder.optionBuilders
        ) {
            optionPredicate = { builder.optionBuilders[it.findDeclarationName()] is UserCommandOptionBuilder }
            optionTransformer = { kParameter, _, resolver ->
                UserContextCommandParameter(kParameter, resolver)
            }
        }
    }

    suspend fun execute(
        context: BContextImpl,
        event: UserContextInteractionEvent
    ): Boolean {
        val arguments: MutableMap<KParameter, Any?> = mutableMapOf()
        arguments[method.instanceParameter!!] = instance
        arguments[method.valueParameters.first()] =
            if (isGuildOnly) GuildUserEvent(method, context, event) else GlobalUserEvent(method, context, event)

        for (parameter in parameters) {
            arguments[parameter.kParameter] = when (parameter.methodParameterType) {
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

        method.callSuspendBy(arguments)

        return true
    }
}