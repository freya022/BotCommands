package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.application.slash.GlobalSlashEvent
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.ApplicationGeneratedOption
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.getCheckedDefaultValue
import com.freya02.botcommands.internal.core.CooldownService
import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.core.options.OptionType
import com.freya02.botcommands.internal.core.reflection.checkEventScope
import com.freya02.botcommands.internal.core.reflection.toMemberEventFunction
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.utils.InsertOptionResult
import com.freya02.botcommands.internal.utils.mapFinalParameters
import com.freya02.botcommands.internal.utils.mapOptions
import com.freya02.botcommands.internal.utils.tryInsertNullableOption
import dev.minn.jda.ktx.messages.reply_
import mu.KotlinLogging
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.jvm.jvmErasure

private val logger = KotlinLogging.logger { }

abstract class SlashCommandInfo internal constructor(
    val context: BContextImpl,
    builder: SlashCommandBuilder
) : ApplicationCommandInfo(
    builder
) {
    val description: String = builder.description

    final override val eventFunction = builder.toMemberEventFunction<GlobalSlashEvent, _>(context)
    final override val parameters: List<SlashCommandParameter>

    init {
        eventFunction.checkEventScope<GuildSlashEvent>(builder)

        parameters = builder.optionAggregateBuilders.transform {
            SlashCommandParameter(this@SlashCommandInfo, builder.optionAggregateBuilders, it)
        }
    }

    internal suspend fun execute(jdaEvent: SlashCommandInteractionEvent, cooldownService: CooldownService): Boolean {
        val event = when {
            topLevelInstance.isGuildOnly -> GuildSlashEvent(context, jdaEvent)
            else -> GlobalSlashEventImpl(context, jdaEvent)
        }

        val objects = getSlashOptions(event, parameters) ?: return false

        cooldownService.applyCooldown(this, event)

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
                    val resolved = option.resolver.resolveSuspend(context, this, event, optionMapping)
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
                } else {
                    null
                }
            }
            OptionType.CUSTOM -> {
                option as CustomMethodOption

                option.resolver.resolveSuspend(context, this, event)
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