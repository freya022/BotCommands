package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.builder.DeclarationSite
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.commands.text.*
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandVariationBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.localization.text.LocalizableTextCommand
import io.github.freya022.botcommands.internal.ExecutableMixin
import io.github.freya022.botcommands.internal.commands.application.slash.SlashUtils.getCheckedDefaultValue
import io.github.freya022.botcommands.internal.core.options.OptionImpl
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

internal class TextCommandVariationImpl internal constructor(
    override val context: BContext,
    builder: TextCommandVariationBuilder
) : TextCommandVariation,
    ExecutableMixin {

    override val declarationSite: DeclarationSite = builder.declarationSite
    override val eventFunction = builder.toMemberParamFunction<BaseCommandEvent, _>(context)
    override val parameters: List<TextCommandParameterImpl>

    val filters: List<TextCommandFilter<*>> = builder.filters.onEach { filter ->
        require(!filter.global) {
            "Global filter ${filter.javaClass.simpleNestedName} cannot be used explicitly, see ${Filter::global.reference}"
        }
    }

    override fun hasFilters(): Boolean = filters.isNotEmpty()

    override val description: String? = builder.description
    override val usage: String? = builder.usage
    override val example: String? = builder.example

    override val completePattern: Regex?

    private val useTokenizedEvent: Boolean

    init {
        useTokenizedEvent = eventFunction.firstParameter.type.jvmErasure.isSubclassOf<CommandEvent>()

        parameters = builder.optionAggregateBuilders.transform {
            TextCommandParameterImpl(context, this, it)
        }

        completePattern = when {
            parameters.flatMap { it.allOptions }.any { it.optionType == OptionType.OPTION } -> CommandPattern.of(this)
            else -> null
        }

        completePattern?.let {
            logger.trace { "Registered text command variation '$it': ${function.shortSignature}" }
        }
    }

    internal suspend fun createEvent(jdaEvent: MessageReceivedEvent, args: String, cancellableRateLimit: CancellableRateLimit, localizableTextCommand: LocalizableTextCommand): BaseCommandEvent = when {
        useTokenizedEvent -> CommandEventImpl.create(context, jdaEvent, args, cancellableRateLimit, localizableTextCommand)
        else -> BaseCommandEventImpl(context, jdaEvent, args, cancellableRateLimit, localizableTextCommand)
    }

    internal suspend fun tryParseOptionValues(event: BaseCommandEvent, args: String, matchResult: MatchResult?): Map<OptionImpl, Any?>? {
        val groupsIterator = matchResult?.groups?.iterator()
        groupsIterator?.next() //Skip entire match

        return parameters.mapOptions { option ->
            if (tryInsertOption(event, this, option, groupsIterator, args) == InsertOptionResult.ABORT)
                return null
        }
    }

    internal suspend fun execute(event: BaseCommandEvent, optionValues: Map<out OptionImpl, Any?>): ExecutionResult {
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
        optionMap: MutableMap<OptionImpl, Any?>,
        option: OptionImpl,
        groupsIterator: Iterator<MatchGroup?>?,
        args: String
    ): InsertOptionResult {
        val value = when (option.optionType) {
            OptionType.OPTION -> {
                groupsIterator ?: throwInternal("No group iterator passed for a regex-based text command")

                option as TextCommandOptionImpl

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
                    throwInternal(option.typeCheckingFunction, "Could not find parameter #${option.index} (${option.helpName}) for input args '${args}', yet the pattern matched and the option is required")
                } else { //Parameter is optional
                    if (option.isOptional) {
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
