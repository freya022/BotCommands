package com.freya02.botcommands.internal.application.context.message

import com.freya02.botcommands.api.application.builder.MessageCommandBuilder
import com.freya02.botcommands.api.application.builder.MessageCommandOptionBuilder
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent
import com.freya02.botcommands.api.application.context.message.GuildMessageEvent
import com.freya02.botcommands.api.parameters.MessageContextParameterResolver
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.application.slash.GeneratedMethodParameter
import com.freya02.botcommands.internal.application.slash.SlashUtils2.checkEventScope
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

class MessageCommandInfo internal constructor(
    context: BContextImpl,
    builder: MessageCommandBuilder
) : ApplicationCommandInfo(context, builder) {
    override val parameters: MethodParameters

    init {
        requireFirstParam(method.valueParameters, GlobalMessageEvent::class)

        checkEventScope<GuildMessageEvent>()

        @Suppress("RemoveExplicitTypeArguments") //Compiler bug
        parameters = MethodParameters2.transform<MessageContextParameterResolver>(
            context,
            method,
            builder.optionBuilders
        ) {
            optionPredicate = { builder.optionBuilders[it.findDeclarationName()] is MessageCommandOptionBuilder }
            optionTransformer = { kParameter, _, resolver ->
                MessageContextCommandParameter(kParameter, resolver)
            }
        }
    }

    @Throws(Exception::class)
    suspend fun execute(
        context: BContextImpl,
        event: MessageContextInteractionEvent
    ): Boolean {
        val arguments: MutableMap<KParameter, Any?> = mutableMapOf()
        arguments[method.instanceParameter!!] = instance
        arguments[method.valueParameters.first()] =
            if (isGuildOnly) GuildMessageEvent(method, context, event) else GlobalMessageEvent(method, context, event)

        for (parameter in parameters) {
            arguments[parameter.kParameter] = when (parameter.methodParameterType) {
                MethodParameterType.COMMAND -> {
                    parameter as MessageContextCommandParameter

                    parameter.resolver.resolve(context, this, event)
                }
                MethodParameterType.CUSTOM -> {
                    parameter as CustomMethodParameter

                    parameter.resolver.resolve(context, this, event)
                }
                MethodParameterType.COMPUTED -> {
                    parameter as GeneratedMethodParameter

                    parameter.generatedOptionBuilder.generatedValueSupplier.getDefaultValue(event)
                }
                else -> TODO()
            }
        }

        applyCooldown(event)

        method.callSuspendBy(arguments)

        return true
    }
}