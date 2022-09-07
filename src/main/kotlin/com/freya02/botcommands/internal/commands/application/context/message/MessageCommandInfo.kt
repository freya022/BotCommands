package com.freya02.botcommands.internal.commands.application.context.message

import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandOptionBuilder
import com.freya02.botcommands.api.commands.application.context.message.GlobalMessageEvent
import com.freya02.botcommands.api.commands.application.context.message.GuildMessageEvent
import com.freya02.botcommands.api.parameters.MessageContextParameterResolver
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.context.message.mixins.ITopLevelMessageCommandInfo
import com.freya02.botcommands.internal.commands.application.context.message.mixins.TopLevelMessageCommandInfoMixin
import com.freya02.botcommands.internal.commands.application.mixins.ITopLevelApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.ApplicationGeneratedMethodParameter
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.checkDefaultValue
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.checkEventScope
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
) : ApplicationCommandInfo(context, builder), ITopLevelMessageCommandInfo by TopLevelMessageCommandInfoMixin(context, builder) {
    override val topLevelInstance: ITopLevelApplicationCommandInfo = this
    override val parentInstance = null
    override val parameters: MethodParameters

    init {
        requireFirstParam(method.valueParameters, GlobalMessageEvent::class)

        builder.checkEventScope<GuildMessageEvent>()

        @Suppress("RemoveExplicitTypeArguments") //Compiler bug
        parameters = MethodParameters.transform<MessageContextParameterResolver<*, *>>(
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
                MethodParameterType.OPTION -> {
                    parameter as MessageContextCommandParameter

                    parameter.resolver.resolveSuspend(context, this, event)
                }
                MethodParameterType.CUSTOM -> {
                    parameter as CustomMethodParameter

                    parameter.resolver.resolveSuspend(context, this, event)
                }
                MethodParameterType.GENERATED -> {
                    parameter as ApplicationGeneratedMethodParameter

                    parameter.generatedValueSupplier.getDefaultValue(event).also { checkDefaultValue(parameter, it) }
                }
                else -> throwInternal("MethodParameterType#${parameter.methodParameterType} has not been implemented")
            }
        }

        applyCooldown(event)

        method.callSuspendBy(arguments)

        return true
    }
}