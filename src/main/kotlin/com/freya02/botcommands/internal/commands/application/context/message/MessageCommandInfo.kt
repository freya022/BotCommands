package com.freya02.botcommands.internal.commands.application.context.message

import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandBuilder
import com.freya02.botcommands.api.commands.application.context.builder.MessageCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.context.message.GlobalMessageEvent
import com.freya02.botcommands.api.commands.application.context.message.GuildMessageEvent
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.ApplicationGeneratedMethodParameter
import com.freya02.botcommands.internal.commands.application.context.message.mixins.ITopLevelMessageCommandInfo
import com.freya02.botcommands.internal.commands.application.context.message.mixins.TopLevelMessageCommandInfoMixin
import com.freya02.botcommands.internal.commands.application.mixins.ITopLevelApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.checkDefaultValue
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.checkEventScope
import com.freya02.botcommands.internal.core.CooldownService
import com.freya02.botcommands.internal.core.options.MethodParameterType
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import com.freya02.botcommands.internal.utils.set
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

class MessageCommandInfo internal constructor(
    context: BContextImpl,
    builder: MessageCommandBuilder
) : ApplicationCommandInfo(context, builder),
    ITopLevelMessageCommandInfo by TopLevelMessageCommandInfoMixin(context, builder) {

    override val topLevelInstance: ITopLevelApplicationCommandInfo = this
    override val parentInstance = null
    override val parameters: List<MessageContextCommandParameter>

    init {
        requireFirstParam(method.valueParameters, GlobalMessageEvent::class)

        builder.checkEventScope<GuildMessageEvent>()

        parameters = builder.optionAggregateBuilders.transform<MessageCommandOptionAggregateBuilder, _> {
            MessageContextCommandParameter(context, it)
        }
    }

    internal suspend fun execute(
        context: BContextImpl,
        cooldownService: CooldownService,
        jdaEvent: MessageContextInteractionEvent
    ): Boolean {
        val event = when {
            isGuildOnly -> GuildMessageEvent(context, jdaEvent)
            else -> GlobalMessageEvent(context, jdaEvent)
        }

        val arguments: MutableMap<KParameter, Any?> = mutableMapOf()
        arguments[method.instanceParameter!!] = instance
        arguments[method.valueParameters.first()] = event

        for (parameter in parameters) {
            val value = computeAggregate(context, event, parameter)

            if (value == null && parameter.kParameter.isOptional) { //Kotlin optional, continue getting more parameters
                continue
            } else if (value == null && !parameter.isOptional) { // Not a kotlin optional and not nullable
                throwUser(parameter.kParameter.function, "Parameter '${parameter.kParameter.bestName}' is not nullable but its aggregator returned null")
            }

            arguments[parameter.kParameter] = value
        }

        cooldownService.applyCooldown(this, event)

        method.callSuspendBy(arguments)

        return true
    }

    private suspend fun computeAggregate(context: BContextImpl, event: GlobalMessageEvent, parameter: MessageContextCommandParameter): Any? {
        val aggregator = parameter.aggregator
        val arguments: MutableMap<KParameter, Any?> = mutableMapOf()
        arguments[aggregator.instanceParameter!!] = parameter.aggregatorInstance
        arguments[aggregator.valueParameters.first()] = event

        for (option in parameter.commandOptions) {
            val value = when (option.methodParameterType) {
                MethodParameterType.OPTION -> {
                    option as MessageContextCommandOption

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

            arguments[option] = value
        }

        return aggregator.callSuspendBy(arguments)
    }
}