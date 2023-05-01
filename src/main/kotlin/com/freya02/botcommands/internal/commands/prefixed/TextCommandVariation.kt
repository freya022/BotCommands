package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.CommandEvent
import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandVariationBuilder
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.ExecutableInteractionInfo
import com.freya02.botcommands.internal.core.CooldownService
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignatureNoSrc
import com.freya02.botcommands.internal.utils.set
import mu.KotlinLogging
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

class TextCommandVariation internal constructor(
    private val context: BContextImpl,
    val info: TextCommandInfo,
    builder: TextCommandVariationBuilder
) : IExecutableInteractionInfo by ExecutableInteractionInfo(context, builder) {
    override val parameters: List<TextCommandParameter>

    val completePattern: Regex?

    private val useTokenizedEvent: Boolean

    init {
        useTokenizedEvent = method.valueParameters.first().type.jvmErasure.isSubclassOf(CommandEvent::class)

        parameters = builder.optionAggregateBuilders.transform<TextCommandOptionAggregateBuilder, _> {
            TextCommandParameter(context, it)
        }

        completePattern = when {
            parameters.any { it.isOption } -> CommandPattern.of(this)
            else -> null
        }
    }

    internal suspend fun execute(
        jdaEvent: MessageReceivedEvent,
        cooldownService: CooldownService,
        args: String,
        matchResult: MatchResult?
    ): ExecutionResult {
        val event = when {
            useTokenizedEvent -> CommandEventImpl.create(context, jdaEvent, args)
            else -> BaseCommandEventImpl(context, jdaEvent, args)
        }

        val objects: MutableMap<KParameter, Any?> = hashMapOf()
        objects[method.instanceParameter!!] = instance
        objects[method.nonInstanceParameters.first()] = event

        val groupsIterator = matchResult?.groups?.iterator()
        for (parameter in parameters) {
            val (value, result) = computeAggregate(event, parameter, groupsIterator, args)
            if (result != null)
                return result

            objects[parameter.kParameter] = value
        }

        cooldownService.applyCooldown(info, event)

        method.callSuspendBy(objects)

        return ExecutionResult.OK
    }

    private suspend fun computeAggregate(
        event: BaseCommandEvent,
        parameter: TextCommandParameter,
        groupsIterator: Iterator<MatchGroup?>?,
        args: String
    ): Pair<Any?, ExecutionResult?> {
        val aggregator = parameter.aggregator
        val aggregatorArguments: MutableMap<KParameter, Any?> = mutableMapOf()
        aggregatorArguments[aggregator.instanceParameter!!] = parameter.aggregatorInstance
        aggregatorArguments[aggregator.valueParameters.first()] = event

        for (option in parameter.commandOptions) {
            aggregatorArguments[option] = when (option.methodParameterType) {
                MethodParameterType.OPTION -> {
                    groupsIterator ?: throwInternal("No group iterator passed for a regex command")

                    option as TextCommandOption

                    var found = 0
                    val groupCount = option.groupCount
                    val groups = arrayOfNulls<String>(groupCount)
                    for (j in 0 until groupCount) {
                        groups[j] = groupsIterator.next()?.value.also {
                            if (it != null) found++
                        }
                    }

                    if (found == groupCount) { //Found all the groups
                        val resolved = option.resolver.resolveSuspend(event.context, this, event, groups)
                        //Regex matched but could not be resolved
                        // if optional then it's ok
                        if (resolved == null && !option.isOptional) {
                            return Pair(null, ExecutionResult.CONTINUE)
                        }

                        resolved
                    } else if (!option.isOptional) { //Parameter is not found yet the pattern matched and is not optional
                        logger.warn(
                            "Could not find parameter #{} in {} for input args {}",
                            option.index,
                            aggregator.shortSignatureNoSrc,
                            args
                        )

                        return Pair(null, ExecutionResult.CONTINUE)
                    } else { //Parameter is optional
                        if (option.kParameter.isOptional) {
                            continue
                        }

                        when {
                            option.isPrimitive -> 0
                            else -> null
                        }
                    }
                }
                MethodParameterType.CUSTOM -> {
                    option as CustomMethodOption

                    option.resolver.resolveSuspend(event.context, this, event)
                }
                MethodParameterType.GENERATED -> {
                    option as TextGeneratedMethodParameter

                    option.generatedValueSupplier.getDefaultValue(event)
                }
                else -> throwInternal("MethodParameterType#${option.methodParameterType} has not been implemented")
            }
        }

        return Pair(aggregator.callSuspendBy(aggregatorArguments), null)
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
