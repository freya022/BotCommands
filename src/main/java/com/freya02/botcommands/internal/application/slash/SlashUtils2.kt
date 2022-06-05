package com.freya02.botcommands.internal.application.slash

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.internal.ExecutableInteractionInfo
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.parameters.resolvers.channels.ChannelResolver
import com.freya02.botcommands.internal.requireUser
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.math.max
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure

object SlashUtils2 {
    @JvmStatic
    fun ExecutableInteractionInfo.checkDefaultValue(
        parameter: AbstractSlashCommandParameter,
        defaultValue: Any?
    ) {
        requireUser(defaultValue != null || parameter.isOptional) {
            "Default value supplier for parameter #${parameter.index} has returned a null value but parameter is not optional"
        }

        if (defaultValue == null) return

        val expectedType: KClass<*> = if (parameter.isVarArg) List::class else parameter.type.jvmErasure

        requireUser(expectedType.isSuperclassOf(defaultValue::class)) {
            "Default value supplier for parameter #${parameter.index} has returned a default value of type ${defaultValue::class.simpleName} but a value of type ${expectedType.simpleName} was expected"
        }

        if (parameter.isVarArg && defaultValue is List<*>) {
            //Check if first parameter exists
            requireUser(defaultValue.firstOrNull() != null) {
                "Default value supplier for parameter #${parameter.index} in %s has returned either an empty list or a list with the first element being null"
            }
        }
    }

    @JvmStatic
    fun SlashCommandInfo.getMethodOptions(context: BContext, guild: Guild?): List<OptionData> {
        val list: MutableList<OptionData> = ArrayList()

        var i = 0
		for (parameter in parameters) {
            if (!parameter.isOption) continue

            if (parameter.methodParameterType != MethodParameterType.COMMAND) continue

            parameter as SlashCommandParameter

            i++

            val name = parameter.discordName
            val description = parameter.description

            if (guild != null) {
                //TODO change to use opaque user data
                val defaultValueSupplier = (instance as ApplicationCommand).getDefaultValueSupplier(
                    context,
                    guild,
                    commandId,
                    path,
                    parameter.name,
                    parameter.type,
                    parameter.type.jvmErasure
                )

                parameter.defaultOptionSupplierMap.put(guild.idLong, defaultValueSupplier)

                if (defaultValueSupplier != null) {
                    continue  //Skip option generation since this is a default value
                }
            }

            val resolver = parameter.resolver
            val optionType = resolver.optionType

            for (varArgNum in 0 until max(1, parameter.varArgs)) {
                val varArgName = SlashUtils.getVarArgName(name, varArgNum)

                val data = OptionData(optionType, varArgName, description)

                when (optionType) {
                    OptionType.CHANNEL -> {
                        //If there are no specified channel types, then try to get the channel type from AbstractChannelResolver
                        // Otherwise set the channel types of the parameter, if available
                        if (parameter.channelTypes.isEmpty() && resolver is ChannelResolver) {
                            data.setChannelTypes(resolver.channelTypes)
                        } else if (parameter.channelTypes.isNotEmpty()) {
                            data.setChannelTypes(parameter.channelTypes)
                        }
                    }
                    OptionType.INTEGER -> {
                        parameter.range?.let {
                            data.setMinValue(it.min.toLong())
                            data.setMaxValue(it.max.toLong())
                        }
                    }
                    OptionType.NUMBER -> {
                        parameter.range?.let {
                            data.setMinValue(it.min.toDouble())
                            data.setMaxValue(it.max.toDouble())
                        }
                    }
                    else -> {}
                }

//                if (applicationOptionData.hasAutocompletion()) { //TODO autocomplete
//                    requireUser(optionType.canSupportChoices()) {
//                        "Slash command parameter #$i does not support autocompletion"
//                    }
//
//                    data.isAutoComplete = true
//                }

                if (optionType.canSupportChoices()) {
                    var choices: Collection<Command.Choice>? = null

                    if (parameter.choices != null) {
                        choices = parameter.choices
                    } else {
                        val predefinedChoices = resolver.getPredefinedChoices(guild)
                        if (predefinedChoices.isNotEmpty()) {
                            choices = predefinedChoices
                        }
                    }

                    if (choices != null) {
//                        requireUser(!applicationOptionData.hasAutocompletion()) { //TODO autocomplete
//                            "Slash command parameter #$i cannot have autocompletion and choices at the same time"
//                        }

                        data.addChoices(choices)
                    }
                }

                //If vararg then next arguments are optional
                data.isRequired = !parameter.isOptional && parameter.isRequiredVararg(varArgNum)

                list.add(data)
            }
        }

        return list
    }
}