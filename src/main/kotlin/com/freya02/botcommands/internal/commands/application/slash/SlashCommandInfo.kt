package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.application.slash.GlobalSlashEvent
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.ApplicationGeneratedMethodParameter
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.checkDefaultValue
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.checkEventScope
import com.freya02.botcommands.internal.core.CooldownService
import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.core.options.OptionType
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.utils.InsertOptionResult
import com.freya02.botcommands.internal.utils.insertAggregate
import dev.minn.jda.ktx.messages.reply_
import mu.KotlinLogging
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import kotlin.collections.set
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

abstract class SlashCommandInfo internal constructor(
    val context: BContextImpl,
    builder: SlashCommandBuilder
) : ApplicationCommandInfo(
    context,
    builder
) {
    val description: String = builder.description

    final override val method: KFunction<*> = super.method
    final override val parameters: List<SlashCommandParameter>

    init {
        requireFirstParam(method.valueParameters, GlobalSlashEvent::class)

        builder.checkEventScope<GuildSlashEvent>()

        parameters = builder.optionAggregateBuilders.transform<SlashCommandOptionAggregateBuilder, _> {
            SlashCommandParameter(this@SlashCommandInfo, builder.optionAggregateBuilders, it)
        }

        //On every autocomplete handler, check if their method parameters match up with the slash command
        parameters.forEach { slashParam ->
            for (commandOption in slashParam.commandOptions) {
                if (commandOption !is SlashCommandOption)
                    continue

                //TODO might need reworking
                commandOption.autocompleteHandler?.let { handler ->
                    handler.methodParameters.forEach { autocompleteParam ->
                        val param = parameters.find { it.name == autocompleteParam.name }
                            ?: throwUser(
                                handler.autocompleteInfo.function,
                                "Could not find parameter ${autocompleteParam.name} in the slash command declaration"
                            )

                        requireUser(
                            param.kParameter.checkTypeEqualsIgnoreNull(autocompleteParam.kParameter),
                            handler.autocompleteInfo.function
                        ) {
                            "Autocomplete parameter type should be the same as the slash command one, slash command type: '${param.type.simpleName}', autocomplete type: '${autocompleteParam.type.simpleName}'"
                        }
                    }
                }
            }
        }
    }

    internal suspend fun execute(jdaEvent: SlashCommandInteractionEvent, cooldownService: CooldownService): Boolean {
        val event = when {
            topLevelInstance.isGuildOnly -> GuildSlashEvent(context, jdaEvent)
            else -> GlobalSlashEventImpl(context, jdaEvent)
        }
        val objects: MutableMap<KParameter, Any?> = mutableMapOf()
        objects[method.instanceParameter!!] = instance
        objects[method.valueParameters.first()] = event

        if (!putSlashOptions(event, objects, parameters)) {
            return false
        }

        cooldownService.applyCooldown(this, event)

        method.callSuspendBy(objects)

        return true
    }

    internal suspend fun <T> putSlashOptions(
        event: T,
        objects: MutableMap<KParameter, Any?>,
        methodParameters: List<AbstractSlashCommandParameter>
    ): Boolean where T : CommandInteractionPayload,
                     T : Event {
        val optionMap: MutableMap<Option, Any?> = hashMapOf()
        for (option in methodParameters.flatMap { it.commandOptions }) {
            if (tryInsertOption(event, optionMap, option) == InsertOptionResult.ABORT)
                return false
        }

        for (parameter in methodParameters) {
            insertAggregate(event, objects, optionMap, parameter)
        }

        return true
    }

    private suspend fun <T> tryInsertOption(
        event: T,
        optionMap: MutableMap<Option, Any?>,
        option: Option
    ): InsertOptionResult where T : CommandInteractionPayload,
                                T : Event {
        when (option.optionType) {
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

                        return InsertOptionResult.ABORT
                    }

                    optionMap[option] = resolved
                } else if (option.isVararg) {
                    //Continue looking at other options
                    return InsertOptionResult.SKIP
                } else if (option.isOptional) { //Default or nullable
                    //Put null/default value if parameter is not a kotlin default value
                    return if (option.kParameter.isOptional) {
                        InsertOptionResult.SKIP //Kotlin default value, don't add anything to the parameters map
                    } else {
                        //Nullable
                        optionMap[option] = when {
                            option.isPrimitive -> 0
                            else -> null
                        }
                        InsertOptionResult.SKIP
                    }
                } else {
                    //TODO might need testing
                    if (event is CommandAutoCompleteInteractionEvent)
                        return InsertOptionResult.SKIP

                    throwUser("Slash parameter couldn't be resolved at option ${option.declaredName} ($optionName)")
                }
            }
            OptionType.CUSTOM -> {
                option as CustomMethodOption

                optionMap[option] = option.resolver.resolveSuspend(context, this, event)
            }
            OptionType.GENERATED -> {
                option as ApplicationGeneratedMethodParameter

                optionMap[option] = option.generatedValueSupplier
                    .getDefaultValue(event)
                    .also { checkDefaultValue(option, it) }
            }
            else -> {
                throwInternal("MethodParameterType#${option.optionType} has not been implemented")
            }
        }

        return InsertOptionResult.OK
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}