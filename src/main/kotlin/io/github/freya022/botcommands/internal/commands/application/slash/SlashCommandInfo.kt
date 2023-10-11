package io.github.freya022.botcommands.internal.commands.application.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.*
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.internal.commands.application.ApplicationGeneratedOption
import io.github.freya022.botcommands.internal.commands.application.slash.SlashUtils.getCheckedDefaultValue
import io.github.freya022.botcommands.internal.core.options.Option
import io.github.freya022.botcommands.internal.core.options.OptionType
import io.github.freya022.botcommands.internal.core.reflection.checkEventScope
import io.github.freya022.botcommands.internal.core.reflection.toMemberEventFunction
import io.github.freya022.botcommands.internal.parameters.CustomMethodOption
import io.github.freya022.botcommands.internal.utils.*
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.jvm.jvmErasure

private val logger = KotlinLogging.logger { }

abstract class SlashCommandInfo internal constructor(
    val context: BContext,
    builder: SlashCommandBuilder
) : ApplicationCommandInfo(
    builder
) {
    val description: String

    final override val eventFunction = builder.toMemberEventFunction<GlobalSlashEvent, _>(context)
    final override val parameters: List<SlashCommandParameter>

    init {
        eventFunction.checkEventScope<GuildSlashEvent>(builder)

        val rootDescription = LocalizationUtils.getCommandRootDescription(context, builder)
        description = if (builder.description != SlashCommandBuilder.DEFAULT_DESCRIPTION) {
            // If a description was set, then use it, but check if a root description was found too
            if (rootDescription != null) {
                logger.debug { "A command description was set manually, while a root description was found in a localization bundle, path: '$path'" }
            }
            builder.description
        } else {
            // If a description isn't set, then take the root description if it exists,
            // otherwise take the builder's default description
            rootDescription ?: builder.description
        }

        parameters = builder.optionAggregateBuilders.transform {
            SlashCommandParameter(this@SlashCommandInfo, builder.optionAggregateBuilders, it)
        }

        parameters
            .flatMap { it.allOptions }
            .filterIsInstance<SlashCommandOption>()
            .forEach(SlashCommandOption::buildAutocomplete)
    }

    internal suspend fun execute(jdaEvent: SlashCommandInteractionEvent, cancellableRateLimit: CancellableRateLimit): Boolean {
        val event = when {
            topLevelInstance.isGuildOnly -> GuildSlashEvent(context, jdaEvent, cancellableRateLimit)
            else -> GlobalSlashEvent(context, jdaEvent, cancellableRateLimit)
        }

        val objects = getSlashOptions(event, parameters) ?: return false
        function.callSuspendBy(objects)

        return true
    }

    context(IExecutableInteractionInfo)
    internal suspend fun <T> getSlashOptions(
        event: T,
        parameters: List<AbstractSlashCommandParameter>
    ): Map<KParameter, Any?>? where T : CommandInteractionPayload, T : Event {
        val optionValues = parameters.mapOptions { option ->
            if (tryInsertOption(event, this, option) == InsertOptionResult.ABORT)
                return null
        }

        return parameters.mapFinalParameters(event, optionValues)
    }

    private suspend fun <T> tryInsertOption(
        event: T,
        optionMap: MutableMap<Option, Any?>,
        option: Option
    ): InsertOptionResult where T : CommandInteractionPayload,
                                T : Event {
        val value = when (option.optionType) {
            OptionType.OPTION -> {
                option as AbstractSlashCommandOption

                val optionName = option.discordName
                val optionMapping = event.getOption(optionName)

                if (optionMapping != null) {
                    val resolved = option.resolver.resolveSuspend(this, event, optionMapping)
                    if (resolved == null) {
                        //Only use the generic message if the user didn't handle this situation
                        if (!event.isAcknowledged && event is SlashCommandInteractionEvent) {
                            event.reply_(
                                context.getDefaultMessages(event).getSlashCommandUnresolvableOptionMsg(option.discordName),
                                ephemeral = true
                            ).queue()
                        }

                        //Not a warning, could be normal if the user did not supply a valid string for user-defined resolvers
                        logger.trace {
                            "The parameter '${option.declaredName}' of value '${optionMapping.asString}' could not be resolved into a ${option.type.jvmErasure.simpleNestedName}"
                        }

                        return when {
                            option.isOptionalOrNullable || option.isVararg -> InsertOptionResult.SKIP
                            else -> InsertOptionResult.ABORT
                        }
                    }

                    resolved
                } else if (!option.isOptionalOrNullable && event is CommandAutoCompleteInteractionEvent) {
                    return InsertOptionResult.ABORT
                } else {
                    null
                }
            }
            OptionType.CUSTOM -> {
                option as CustomMethodOption

                option.resolver.resolveSuspend(this, event)
            }
            OptionType.GENERATED -> {
                option as ApplicationGeneratedOption

                option.getCheckedDefaultValue { it.generatedValueSupplier.getDefaultValue(event) }
            }
            else -> {
                throwInternal("${option.optionType} has not been implemented")
            }
        }

        return tryInsertNullableOption(value, option, optionMap)
    }
}