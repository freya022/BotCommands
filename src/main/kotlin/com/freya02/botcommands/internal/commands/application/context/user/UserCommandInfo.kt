package com.freya02.botcommands.internal.commands.application.context.user

import com.freya02.botcommands.api.commands.application.context.builder.UserCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.UserCommandOptionAggregateBuilder
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
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
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
    override val parameters: List<UserContextCommandParameter>

    init {
        requireFirstParam(method.valueParameters, GlobalUserEvent::class)

        builder.checkEventScope<GuildUserEvent>()

        parameters = builder.optionAggregateBuilders.transform<UserCommandOptionAggregateBuilder, _> {
            UserContextCommandParameter(context, it)
        }
    }

    internal suspend fun execute(
        context: BContextImpl,
        cooldownService: CooldownService,
        jdaEvent: UserContextInteractionEvent
    ): Boolean {
        val event = when {
            isGuildOnly -> GuildUserEvent(context, jdaEvent)
            else -> GlobalUserEvent(context, jdaEvent)
        }

        val arguments: MutableMap<KParameter, Any?> = mutableMapOf()
        arguments[method.instanceParameter!!] = instance
        arguments[method.valueParameters.first()] = event

        for (parameter in parameters) {
            val value = computeAggregate(context, event, parameter)

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

    private suspend fun computeAggregate(context: BContextImpl, event: GlobalUserEvent, parameter: UserContextCommandParameter): Any? {
        val aggregator = parameter.aggregator
        val arguments: MutableMap<KParameter, Any?> = mutableMapOf()
        arguments[aggregator.instanceParameter!!] = parameter.aggregatorInstance
        arguments[aggregator.valueParameters.first()] = event

        for (option in parameter.commandOptions) {
            val value = when (option.methodParameterType) {
                MethodParameterType.OPTION -> {
                    option as UserContextCommandOption

                    option.resolver.resolveSuspend(context, this, event)
                }
                MethodParameterType.CUSTOM -> {
                    option as CustomMethodOption

                    option.resolver.resolveSuspend(context, this, event)
                }
                MethodParameterType.GENERATED -> {
                    option as ApplicationGeneratedMethodParameter

                    option.generatedValueSupplier.getDefaultValue(event).also { checkDefaultValue(option, it) }
                }
                else -> throwInternal("MethodParameterType#${option.methodParameterType} has not been implemented")
            }

            if (value == null && option.kParameter.isOptional) { //Kotlin optional, continue getting more parameters
                continue
            } else if (value == null && !option.isOptional) { // Not a kotlin optional and not nullable
                throwUser(option.kParameter.function, "Parameter '${option.kParameter.bestName}' is not nullable but its resolver returned null")
            }

            arguments[option.executableParameter] = value
        }

        return aggregator.callSuspendBy(arguments)
    }
}