package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.commands.application.builder.OptionBuilder.Companion.findOption
import com.freya02.botcommands.api.commands.prefixed.CommandEvent
import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandVariationBuilder
import com.freya02.botcommands.api.commands.prefixed.builder.TextOptionBuilder
import com.freya02.botcommands.api.parameters.RegexParameterResolver
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.ExecutableInteractionInfo
import com.freya02.botcommands.internal.commands.ExecutableInteractionInfo.Companion.filterOptions
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import com.freya02.botcommands.internal.utils.Utils
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.regex.Matcher
import java.util.regex.Pattern
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
    override val parameters: MethodParameters
    override val optionParameters: List<TextCommandParameter>

    val completePattern: Pattern?

    private val useTokenizedEvent: Boolean

    init {
        useTokenizedEvent = method.valueParameters.first().type.jvmErasure.isSubclassOf(CommandEvent::class)

        @Suppress("RemoveExplicitTypeArguments") //Compiler bug
        parameters = MethodParameters.transform<RegexParameterResolver<*, *>>(
            context,
            method,
            builder.optionBuilders
        ) {
            optionPredicate = { builder.optionBuilders[it.findDeclarationName()] is TextOptionBuilder }
            optionTransformer = { parameter, paramName, resolver -> TextCommandParameter(parameter, builder.optionBuilders.findOption(paramName), resolver) }
        }

        optionParameters = parameters.filterOptions()

        completePattern = when {
            parameters.any { it.isOption } -> CommandPattern.of(this)
            else -> null
        }
    }

    suspend fun execute(
        _event: MessageReceivedEvent,
        args: String,
        matcher: Matcher?
    ): ExecutionResult {
        val event = when {
            useTokenizedEvent -> CommandEventImpl.create(context, method, _event, args)
            else -> BaseCommandEventImpl(context, method, _event, args)
        }

        val objects: MutableMap<KParameter, Any?> = hashMapOf()
        objects[method.instanceParameter!!] = instance
        objects[method.nonInstanceParameters.first()] = event

        var groupIndex = 1
        parameterLoop@for (parameter in parameters) {
            objects[parameter.kParameter] = when (parameter.methodParameterType) {
                MethodParameterType.OPTION -> {
                    matcher ?: throwInternal("No matcher passed for a regex command")

                    parameter as TextCommandParameter

                    var found = 0
                    val groupCount = parameter.groupCount
                    val groups = arrayOfNulls<String>(groupCount)
                    for (j in 0 until groupCount) {
                        groups[j] = matcher.group(groupIndex++)
                        if (groups[j] != null) found++
                    }

                    if (found == groupCount) { //Found all the groups
                        val resolved = parameter.resolver.resolveSuspend(context, this, event, groups)
                        //Regex matched but could not be resolved
                        // if optional then it's ok
                        if (resolved == null && !parameter.isOptional) {
                            return ExecutionResult.CONTINUE
                        }

                        resolved
                    } else if (!parameter.isOptional) { //Parameter is not found yet the pattern matched and is not optional
                        LOGGER.warn(
                            "Could not find parameter #{} in {} for input args {}",
                            parameter.index,
                            Utils.formatMethodShort(method),
                            args
                        )

                        return ExecutionResult.CONTINUE
                    } else { //Parameter is optional
                        if (parameter.kParameter.isOptional) {
                            continue@parameterLoop
                        }

                        when {
                            parameter.isPrimitive -> 0
                            else -> null
                        }
                    }
                }
                MethodParameterType.CUSTOM -> {
                    parameter as CustomMethodParameter

                    parameter.resolver.resolveSuspend(context, this, event)
                }
                MethodParameterType.GENERATED -> {
                    parameter as TextGeneratedMethodParameter

                    parameter.generatedValueSupplier.getDefaultValue(event)
                }
                else -> throwInternal("MethodParameterType#${parameter.methodParameterType} has not been implemented")
            }
        }

        info.applyCooldown(event) //TODO cooldown is applied on a per-alternative basis, it should be per command path

        method.callSuspendBy(objects)

        return ExecutionResult.OK
    }

    companion object {
        private val LOGGER = Logging.getLogger()
    }
}