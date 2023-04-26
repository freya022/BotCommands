package com.freya02.botcommands.internal.commands.application.context.user

import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandOptionBuilder
import com.freya02.botcommands.api.commands.application.context.user.GlobalUserEvent
import com.freya02.botcommands.api.commands.application.context.user.GuildUserEvent
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.ApplicationGeneratedMethodParameter
import com.freya02.botcommands.internal.commands.application.context.user.mixins.ITopLevelUserCommandInfo
import com.freya02.botcommands.internal.commands.application.context.user.mixins.TopLevelUserCommandInfoMixin
import com.freya02.botcommands.internal.commands.application.mixins.ITopLevelApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.checkDefaultValue
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.checkEventScope
import com.freya02.botcommands.internal.core.CooldownService
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
) : ApplicationCommandInfo(context, builder),
    ITopLevelUserCommandInfo by TopLevelUserCommandInfoMixin(context, builder) {

    override val topLevelInstance: ITopLevelApplicationCommandInfo = this
    override val parentInstance = null
    override val parameters: MethodParameters

    init {
        requireFirstParam(method.valueParameters, GlobalUserEvent::class)

        builder.checkEventScope<GuildUserEvent>()

        @Suppress("RemoveExplicitTypeArguments") //Compiler bug
        parameters = MethodParameters.transform(
            builder.commandOptionBuilders
        ) {
            optionPredicate = { builder.commandOptionBuilders[it.findDeclarationName()] is UserCommandOptionBuilder }
            optionTransformer = { kParameter, _, resolver ->
                UserContextCommandParameter(kParameter, resolver)
            }
        }
    }

    internal suspend fun execute(
        context: BContextImpl,
        cooldownService: CooldownService,
        event: UserContextInteractionEvent
    ): Boolean {
        val arguments: MutableMap<KParameter, Any?> = mutableMapOf()
        arguments[method.instanceParameter!!] = instance
        arguments[method.valueParameters.first()] =
            if (isGuildOnly) GuildUserEvent(context, event) else GlobalUserEvent(context, event)

        for (parameter in parameters) {
            val value = when (parameter.methodParameterType) {
                MethodParameterType.OPTION -> {
                    parameter as UserContextCommandParameter

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

            if (value == null && parameter.kParameter.isOptional) { //Kotlin optional, continue getting more parameters
                continue
            } else if (value == null && !parameter.isOptional) { // Not a kotlin optional and not nullable
                throwUser("Parameter '${parameter.kParameter.bestName}' is not nullable but its resolver returned null")
            }

            arguments[parameter.kParameter] = value
        }

        cooldownService.applyCooldown(this, event)

        method.callSuspendBy(arguments)

        return true
    }
}