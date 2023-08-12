package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.CommandEvent
import com.freya02.botcommands.api.commands.prefixed.TextCommandFilter
import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandVariationBuilder
import com.freya02.botcommands.api.core.CooldownService
import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.getCheckedDefaultValue
import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.core.options.OptionType
import com.freya02.botcommands.internal.core.reflection.toMemberEventFunction
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.transform
import com.freya02.botcommands.internal.utils.*
import mu.KotlinLogging
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

private val logger = KotlinLogging.logger { }

class TextCommandVariation internal constructor(
    private val context: BContextImpl,
    val info: TextCommandInfo,
    builder: TextCommandVariationBuilder
) : IExecutableInteractionInfo {
    override val eventFunction = builder.toMemberEventFunction<BaseCommandEvent, _>(context)
    override val parameters: List<TextCommandParameter>

    val completePattern: Regex?

    private val useTokenizedEvent: Boolean

    init {
        useTokenizedEvent = eventFunction.eventParameter.type.jvmErasure.isSubclassOf(CommandEvent::class)

        parameters = builder.optionAggregateBuilders.transform {
            TextCommandParameter(context, it)
        }

        completePattern = when {
            parameters.flatMap { it.allOptions }.any { it.optionType == OptionType.OPTION } -> CommandPattern.of(this)
            else -> null
        }
    }

    internal suspend fun execute(
        jdaEvent: MessageReceivedEvent,
        cooldownService: CooldownService,
        filters: List<TextCommandFilter>,
        content: String,
        args: String,
        matchResult: MatchResult?
    ): ExecutionResult {
        val event = when {
            useTokenizedEvent -> CommandEventImpl.create(context, jdaEvent, args)
            else -> BaseCommandEventImpl(context, jdaEvent, args)
        }

        val groupsIterator = matchResult?.groups?.iterator()
        groupsIterator?.next() //Skip entire match

        val optionValues = parameters.mapOptions { option ->
            if (tryInsertOption(event, this, option, groupsIterator, args) == InsertOptionResult.ABORT)
                return ExecutionResult.CONTINUE //Go to next variation
        }

        for (filter in filters) {
            if (!filter.isAccepted(event, this.info, args)) {
                logger.trace { "${filter::class.simpleNestedName} rejected text command '$content'" }
                return ExecutionResult.STOP
            }
        }

        cooldownService.applyCooldown(info, event)

        function.callSuspendBy(parameters.mapFinalParameters(event, optionValues))

        return ExecutionResult.OK
    }

    /**
     * Will return a null value if it can go to the next option
     *
     * A non-null value is returned immediately to #insertAggregate caller
     */
    private suspend fun tryInsertOption(
        event: BaseCommandEvent,
        optionMap: MutableMap<Option, Any?>,
        option: Option,
        groupsIterator: Iterator<MatchGroup?>?,
        args: String
    ): InsertOptionResult {
        val value = when (option.optionType) {
            OptionType.OPTION -> {
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
                    if (resolved == null && !option.isOptionalOrNullable) {
                        return InsertOptionResult.SKIP
                    }

                    resolved
                } else if (!option.isOptionalOrNullable) { //Parameter is not found yet the pattern matched and is not optional
                    throwInternal(option.optionParameter.typeCheckingFunction, "Could not find parameter #${option.index} (${option.helpName}) for input args '${args}', yet the pattern matched and the option is required")
                } else { //Parameter is optional
                    if (option.kParameter.isOptional) {
                        return InsertOptionResult.OK
                    }

                    option.nullValue
                }
            }

            OptionType.CUSTOM -> {
                option as CustomMethodOption

                option.resolver.resolveSuspend(event.context, this, event)
            }

            OptionType.GENERATED -> {
                option as TextGeneratedOption

                option.getCheckedDefaultValue { it.generatedValueSupplier.getDefaultValue(event) }
            }

            else -> throwInternal("${option.optionType} has not been implemented")
        }

        return tryInsertNullableOption(value, option, optionMap)
    }
}
