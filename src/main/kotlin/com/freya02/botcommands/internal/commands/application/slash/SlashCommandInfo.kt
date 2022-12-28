package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.commands.application.builder.OptionBuilder.Companion.findOption
import com.freya02.botcommands.api.commands.application.slash.GlobalSlashEvent
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.application.ApplicationGeneratedMethodParameter
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.checkDefaultValue
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.checkEventScope
import com.freya02.botcommands.internal.commands.application.slash.SlashUtils.toVarArgName
import com.freya02.botcommands.internal.core.CooldownService
import com.freya02.botcommands.internal.parameters.CustomMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import kotlin.math.max
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
    final override val parameters: MethodParameters

    @Suppress("UNCHECKED_CAST")
    override val optionParameters: List<SlashCommandParameter>
        get() = super.optionParameters as List<SlashCommandParameter>

    init {
        requireFirstParam(method.valueParameters, GlobalSlashEvent::class)

        builder.checkEventScope<GuildSlashEvent>()

        @Suppress("RemoveExplicitTypeArguments") //Compiler bug
        parameters = MethodParameters.transform<SlashParameterResolver<*, *>>(
            context,
            method,
            builder.optionBuilders
        ) {
            optionPredicate = { builder.optionBuilders[it.findDeclarationName()] is SlashCommandOptionBuilder }
            optionTransformer = { kParameter, paramName, resolver ->
                val optionBuilder = builder.optionBuilders.findOption<SlashCommandOptionBuilder>(paramName, "a slash command option")
                SlashCommandParameter(this@SlashCommandInfo, builder.optionBuilders, kParameter, optionBuilder, resolver)
            }
        }

        //On every autocomplete handler, check if their method parameters match up with the slash command
        parameters.forEach { slashParam ->
            if (slashParam.methodParameterType == MethodParameterType.OPTION) {
                slashParam as SlashCommandParameter

                slashParam.autocompleteHandler?.let { handler ->
                    handler.methodParameters.forEach { autocompleteParam ->
                        val param = parameters.find { it.name == autocompleteParam.name }
                            ?: throwUser(
                                handler.autocompleteInfo.function,
                                "Could not find parameter ${autocompleteParam.name} in the slash command declaration"
                            )

                        requireUser(param.kParameter.checkTypeEqualsIgnoreNull(autocompleteParam.kParameter), handler.autocompleteInfo.function) {
                            "Autocomplete parameter type should be the same as the slash command one, slash command type: '${param.type.simpleName}', autocomplete type: '${autocompleteParam.type.simpleName}'"
                        }
                    }
                }
            }
        }
    }

    suspend fun execute(event: SlashCommandInteractionEvent, cooldownService: CooldownService): Boolean {
        val objects: MutableMap<KParameter, Any?> = mutableMapOf()
        objects[method.instanceParameter!!] = instance
        objects[method.valueParameters.first()] =
            if (topLevelInstance.isGuildOnly) GuildSlashEvent(context, method, event) else GlobalSlashEventImpl(context, method, event)

        putSlashOptions(event, objects, parameters)

        cooldownService.applyCooldown(this, event)

        method.callSuspendBy(objects)

        return true
    }

    internal suspend fun <T> putSlashOptions(
        event: T,
        objects: MutableMap<KParameter, Any?>,
        methodParameters: MethodParameters
    ) where T : CommandInteractionPayload,
            T : Event {
        parameterLoop@ for (parameter in methodParameters) {
            if (parameter.methodParameterType == MethodParameterType.OPTION) {
                parameter as AbstractSlashCommandParameter

                val arguments = max(1, parameter.varArgs)
                val objectList: MutableList<Any?> = arrayOfSize(arguments)

                val optionName = parameter.discordName
                for (varArgNum in 0 until arguments) {
                    val varArgName = optionName.toVarArgName(varArgNum)
                    val optionMapping = event.getOption(varArgName)
                        //TODO this is probably wrong for out of order varargs
                        ?: if (parameter.isOptional || (parameter.isVarArg && !parameter.isRequiredVararg(varArgNum))) {
                            if (!parameter.kParameter.isOptional) {
                                objectList += when {
                                    parameter.isPrimitive -> 0
                                    else -> null
                                }

                                continue //Continue looking at varargs
                            } else {
                                continue@parameterLoop //Parameter is optional, don't put anything
                            }
                        } else {
                            if (event is CommandAutoCompleteInteractionEvent) continue@parameterLoop

                            throwUser("Slash parameter couldn't be resolved at parameter " + parameter.name + " (" + varArgName + ")")
                        }

                    val resolved = parameter.resolver.resolveSuspend(context, this, event, optionMapping)
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
                        LOGGER.trace(
                            "The parameter '{}' of value '{}' could not be resolved into a {}",
                            parameter.name,
                            optionMapping.asString,
                            parameter.type.jvmErasure.simpleName
                        )

                        return
                    }

                    objectList.add(resolved)
                }

                objects[parameter.kParameter] = if (parameter.isVarArg) objectList else objectList[0]
            } else if (parameter.methodParameterType == MethodParameterType.CUSTOM) {
                parameter as CustomMethodParameter

                objects[parameter.kParameter] = parameter.resolver.resolveSuspend(context, this, event)
            } else if (parameter.methodParameterType == MethodParameterType.GENERATED) {
                parameter as ApplicationGeneratedMethodParameter

                val defaultVal = parameter.generatedValueSupplier.getDefaultValue(event)
                checkDefaultValue(parameter, defaultVal)

                objects[parameter.kParameter] = defaultVal
            } else {
                throwInternal("MethodParameterType#${parameter.methodParameterType} has not been implemented")
            }
        }
    }

    fun getAutocompleteHandlerName(event: CommandAutoCompleteInteractionEvent): String? {
        throw UnsupportedOperationException()
    }

    companion object {
        private val LOGGER = Logging.getLogger()
    }
}