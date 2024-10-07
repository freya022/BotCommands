package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.commands.text.*
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.api.core.Filter
import io.github.freya022.botcommands.api.core.utils.isSubclassOf
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.api.localization.text.LocalizableTextCommand
import io.github.freya022.botcommands.internal.ExecutableMixin
import io.github.freya022.botcommands.internal.commands.application.slash.SlashUtils.getCheckedDefaultValue
import io.github.freya022.botcommands.internal.commands.text.builder.TextCommandVariationBuilderImpl
import io.github.freya022.botcommands.internal.commands.text.options.TextCommandOptionImpl
import io.github.freya022.botcommands.internal.commands.text.options.TextCommandParameterImpl
import io.github.freya022.botcommands.internal.commands.text.options.TextGeneratedOption
import io.github.freya022.botcommands.internal.commands.text.options.builder.TextCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.reflection.toMemberParamFunction
import io.github.freya022.botcommands.internal.options.transform
import io.github.freya022.botcommands.internal.parameters.CustomMethodOption
import io.github.freya022.botcommands.internal.parameters.ServiceMethodOption
import io.github.freya022.botcommands.internal.utils.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.jvm.jvmErasure

internal class TextCommandVariationImpl internal constructor(
    override val context: BContext,
    override val command: TextCommandInfo,
    builder: TextCommandVariationBuilderImpl
) : TextCommandVariation,
    ExecutableMixin {

    override val declarationSite: DeclarationSite = builder.declarationSite
    override val eventFunction = builder.toMemberParamFunction<BaseCommandEvent, _>(context)
    override val parameters: List<TextCommandParameterImpl>

    /**
     * Set of filters preventing this command from executing.
     *
     * @see TextCommandFilter
     * @see TextCommandRejectionHandler
     */
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
            TextCommandParameterImpl(context, this, it as TextCommandOptionAggregateBuilderImpl)
        }

        completePattern = when {
            parameters.flatMap { it.allOptions }.any { it.optionType == OptionType.OPTION } -> CommandPattern.of(this)
            else -> null
        }
    }

    internal suspend fun createEvent(jdaEvent: MessageReceivedEvent, args: String, cancellableRateLimit: CancellableRateLimit, localizableTextCommand: LocalizableTextCommand): BaseCommandEvent = when {
        useTokenizedEvent -> CommandEventImpl.create(context, jdaEvent, args, cancellableRateLimit, localizableTextCommand)
        else -> BaseCommandEventImpl(context, jdaEvent, args, cancellableRateLimit, localizableTextCommand)
    }

    internal suspend fun tryParseOptionValues(event: BaseCommandEvent, matchResult: MatchResult?): Map<OptionImpl, Any?>? {
        val groupsIterator = matchResult?.groups?.iterator()
        groupsIterator?.next() //Skip the entire match

        return parameters.mapOptions { option ->
            if (tryInsertOption(event, this, option, groupsIterator) == InsertOptionResult.ABORT)
                return null
        }
    }

    internal suspend fun execute(event: BaseCommandEvent, optionValues: Map<out OptionImpl, Any?>) {
        val finalParameters = parameters.mapFinalParameters(event, optionValues)

        function.callSuspendBy(finalParameters)
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
        groupsIterator: Iterator<MatchGroup?>?
    ): InsertOptionResult {
        val value = when (option.optionType) {
            OptionType.OPTION -> {
                groupsIterator ?: throwInternal("No group iterator passed for a regex-based text command")

                option as TextCommandOptionImpl

                val groups: Array<String?> = Array(option.groupCount) { groupsIterator.next()?.value }
                option.resolver.resolveSuspend(this, event, groups)
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

        // If value is null and required, go to next variation
        if (value == null && !option.isOptionalOrNullable)
            return InsertOptionResult.ABORT

        return tryInsertNullableOption(value, option, optionMap)
    }
}
