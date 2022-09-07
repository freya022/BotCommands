package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.application.builder.OptionBuilder.Companion.findOption
import com.freya02.botcommands.api.commands.prefixed.CommandEvent
import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandBuilder
import com.freya02.botcommands.api.commands.prefixed.builder.TextOptionBuilder
import com.freya02.botcommands.api.parameters.RegexParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.MethodParameters
import com.freya02.botcommands.internal.commands.AbstractCommandInfo
import com.freya02.botcommands.internal.commands.application.mixins.INamedCommandInfo
import com.freya02.botcommands.internal.findDeclarationName
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.nonInstanceParameters
import com.freya02.botcommands.internal.utils.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.function.Consumer
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.reflect.KParameter
import kotlin.reflect.cast
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

private val LOGGER = Logging.getLogger()

class TextCommandInfo(
    private val context: BContextImpl,
    builder: TextCommandBuilder,
    override val parentInstance: INamedCommandInfo?
) : AbstractCommandInfo(context, builder) {
    override val parameters: MethodParameters
    override val optionParameters: List<TextCommandParameter>
        get() = super.optionParameters.map { TextCommandParameter::class.cast(it) }

    val subcommands: Map<String, List<TextCommandInfo>> = let {
        val map: MutableMap<String, MutableList<TextCommandInfo>> = hashMapOf()

        builder.subcommands.forEach {
            map.computeIfAbsent(it.name) { arrayListOf() }.add(it.build(this))
        }

        map
    }

    val aliases: List<CommandPath> = builder.aliases

    val category: String = builder.category
    val description: String = builder.description

    val isOwnerRequired: Boolean = builder.ownerRequired
    val hidden: Boolean = builder.hidden

    val completePattern: Pattern?
    val order: Int = builder.order

    val detailedDescription: Consumer<EmbedBuilder>? = builder.detailedDescription

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

        applyCooldown(event) //TODO cooldown is applied on a per-alternative basis, it should be per command path

        method.callSuspendBy(objects)

        return ExecutionResult.OK
    }
}