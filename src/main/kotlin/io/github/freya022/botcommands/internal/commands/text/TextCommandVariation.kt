package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.builder.DeclarationSite
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolder
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.CommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommandFilter
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandVariationBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.IExecutableInteractionInfo
import io.github.freya022.botcommands.internal.commands.application.slash.SlashUtils.getCheckedDefaultValue
import io.github.freya022.botcommands.internal.core.options.Option
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.reflection.toMemberParamFunction
import io.github.freya022.botcommands.internal.parameters.CustomMethodOption
import io.github.freya022.botcommands.internal.parameters.ServiceMethodOption
import io.github.freya022.botcommands.internal.transform
import io.github.freya022.botcommands.internal.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.jvm.jvmErasure

private val logger = KotlinLogging.logger { }

class TextCommandVariation internal constructor(
    private val context: BContext,
    val info: TextCommandInfo,
    builder: TextCommandVariationBuilder
) : IExecutableInteractionInfo, IDeclarationSiteHolder {
    override val declarationSite: DeclarationSite = builder.declarationSite
    override val eventFunction = builder.toMemberParamFunction<BaseCommandEvent, _>(context)
    override val parameters: List<TextCommandParameter>

    val filters: List<TextCommandFilter<*>> = builder.filters.onEach { filter ->
        require(!filter.global) {
            "Global filter ${filter.javaClass.simpleNestedName} cannot be used explicitly, see ${Filter::global.reference}"
        }
    }

    val description: String? = builder.description
    val usage: String? = builder.usage
    val example: String? = builder.example

    val completePattern: Regex?

    private val useTokenizedEvent: Boolean

    init {
        useTokenizedEvent = eventFunction.firstParameter.type.jvmErasure.isSubclassOf<CommandEvent>()

        parameters = builder.optionAggregateBuilders.transform {
            TextCommandParameter(context, it)
        }

        completePattern = when {
            parameters.flatMap { it.allOptions }.any { it.optionType == OptionType.OPTION } -> CommandPattern.of(this)
            else -> null
        }

        completePattern?.let {
            logger.trace { "Registered text command variation '$it': ${function.shortSignature}" }
        }
    }

    internal suspend fun createEvent(jdaEvent: MessageReceivedEvent, args: String, cancellableRateLimit: CancellableRateLimit): BaseCommandEvent = when {
        useTokenizedEvent -> CommandEventImpl.create(context, jdaEvent, args, cancellableRateLimit)
        else -> BaseCommandEventImpl(context, jdaEvent, args, cancellableRateLimit)
    }

    internal suspend fun tryParseOptionValues(event: BaseCommandEvent, args: String, matchResult: MatchResult?): Map<Option, Any?>? {
        val groupsIterator = matchResult?.groups?.iterator()
        groupsIterator?.next() //Skip entire match

        return parameters.mapOptions { option ->
            if (tryInsertOption(event, this, option, groupsIterator, args) == InsertOptionResult.ABORT)
                return null
        }
    }

    internal suspend fun execute(
        event: BaseCommandEvent,
        optionValues: Map<Option, Any?>
    ): ExecutionResult {
        val finalParameters = parameters.mapFinalParameters(event, optionValues)

        function.callSuspendBy(finalParameters)

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
                groupsIterator ?: throwInternal("No group iterator passed for a regex-based text command")

                option as TextCommandOption

                var found = 0
                val groupCount = option.groupCount
                val groups = arrayOfNulls<String>(groupCount)
                for (j in 0 until groupCount) {
                    groups[j] = groupsIterator.next()?.value.also {
                        if (it != null) found++
                    }
                }

                if (found >= option.requiredGroups) { //Found all the groups
                    val resolved = option.resolver.resolveSuspend(this, event, groups)
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

                option.resolver.resolveSuspend(this, event)
            }

            OptionType.GENERATED -> {
                option as TextGeneratedOption

                option.getCheckedDefaultValue { it.generatedValueSupplier.getDefaultValue(event) }
            }

            OptionType.SERVICE -> (option as ServiceMethodOption).getService()

            OptionType.CONSTANT -> throwInternal("${option.optionType} has not been implemented")
        }

        return tryInsertNullableOption(value, option, optionMap)
    }
}
