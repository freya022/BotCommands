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
import com.freya02.botcommands.internal.core.options.MethodParameterType
import com.freya02.botcommands.internal.parameters.CustomMethodOption
import com.freya02.botcommands.internal.utils.expandVararg
import com.freya02.botcommands.internal.utils.set
import mu.KotlinLogging
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
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
        parameterLoop@ for (parameter in methodParameters) {
            if (!insertAggregate(context, event, objects, parameter))
                return false
        }

        return true
    }

    private suspend fun <T> insertAggregate(
        context: BContextImpl,
        event: T,
        objects: MutableMap<KParameter, Any?>,
        parameter: AbstractSlashCommandParameter
    ): Boolean where T : CommandInteractionPayload,
                     T : Event {
        val aggregator = parameter.aggregator
        val aggregatorArguments: MutableMap<KParameter, Any?> = mutableMapOf()
        aggregatorArguments[aggregator.instanceParameter!!] = parameter.aggregatorInstance
        aggregatorArguments[aggregator.valueParameters.first()] = event

        for (option in parameter.commandOptions) {
            if (option.methodParameterType == MethodParameterType.OPTION) {
                option as AbstractSlashCommandOption

                val optionName = option.discordName
                val optionMapping = event.getOption(optionName)
                    ?: if (option.isVararg) {
                        continue //Continue looking at other options
                    } else if (parameter.isOptional) { //Default or nullable
                        //Put null/default value if parameter is not a kotlin default value
                        if (parameter.kParameter.isOptional) {
                            continue //Kotlin default value, don't add anything to the parameters map
                        } else {
                            //Nullable
                            aggregatorArguments[option] = when {
                                option.isPrimitive -> 0
                                else -> null
                            }
                            continue
                        }
                    } else {
                        //TODO might need testing
                        if (event is CommandAutoCompleteInteractionEvent) continue

                        throwUser("Slash parameter couldn't be resolved at parameter ${parameter.name} ($optionName)")
                    }

                val resolved = option.resolver.resolveSuspend(context, this, event, optionMapping)
                if (resolved == null) {
                    if (event is SlashCommandInteractionEvent) {
                        event.reply(
                            context.getDefaultMessages(event).getSlashCommandUnresolvableParameterMsg(
                                parameter.name,
                                parameter.type.jvmErasure.simpleName
                            )
                        )
                            .setEphemeral(true)
                            .queue()
                    }

                    //Not a warning, could be normal if the user did not supply a valid string for user-defined resolvers
                    logger.trace(
                        "The parameter '{}' of value '{}' could not be resolved into a {}",
                        parameter.name,
                        optionMapping.asString,
                        parameter.type.jvmErasure.simpleName
                    )

                    return false
                }

                aggregatorArguments[option] = resolved
            } else if (option.methodParameterType == MethodParameterType.CUSTOM) {
                option as CustomMethodOption

                aggregatorArguments[option] = option.resolver.resolveSuspend(context, this, event)
            } else if (option.methodParameterType == MethodParameterType.GENERATED) {
                option as ApplicationGeneratedMethodParameter

                aggregatorArguments[option] = option.generatedValueSupplier
                    .getDefaultValue(event)
                    .also { checkDefaultValue(option, it) }
            } else {
                throwInternal("MethodParameterType#${option.methodParameterType} has not been implemented")
            }
        }

        objects[parameter] = aggregator.callSuspendBy(aggregatorArguments.expandVararg())

        return true
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}