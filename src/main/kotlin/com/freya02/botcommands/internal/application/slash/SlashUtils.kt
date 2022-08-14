package com.freya02.botcommands.internal.application.slash

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.internal.ExecutableInteractionInfo
import com.freya02.botcommands.internal.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.parameters.resolvers.channels.ChannelResolver
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignature
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.math.max
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

object SlashUtils {
    fun ExecutableInteractionInfo.checkDefaultValue(
        parameter: ApplicationGeneratedMethodParameter,
        defaultValue: Any?
    ) {
        requireUser(defaultValue != null || parameter.isOptional) {
            "Generated value supplier for parameter #${parameter.index} has returned a null value but parameter is not optional"
        }

        if (defaultValue == null) return

        val expectedType: KClass<*> = parameter.type.jvmErasure

        requireUser(expectedType.isSuperclassOf(defaultValue::class)) {
            "Generated value supplier for parameter #${parameter.index} has returned a default value of type ${defaultValue::class.simpleName} but a value of type ${expectedType.simpleName} was expected"
        }
    }

    @JvmStatic
    fun SlashCommandInfo.getMethodOptions(guild: Guild?): List<OptionData> {
        val list: MutableList<OptionData> = ArrayList()

        var i = 0
		for (parameter in parameters) {
            if (!parameter.isOption) continue

            if (parameter.methodParameterType != MethodParameterType.OPTION) continue

            parameter as SlashCommandParameter

            i++

            val name = parameter.discordName
            val description = parameter.description

            val resolver = parameter.resolver
            val optionType = resolver.optionType

            for (varArgNum in 0 until max(1, parameter.varArgs)) {
                val varArgName = name.toVarArgName(varArgNum)

                val data = OptionData(optionType, varArgName, description)

                when (optionType) {
                    OptionType.CHANNEL -> {
                        //If there are no specified channel types, then try to get the channel type from AbstractChannelResolver
                        // Otherwise set the channel types of the parameter, if available
                        if (parameter.channelTypes == null && resolver is ChannelResolver) {
                            data.setChannelTypes(resolver.channelTypes)
                        } else if (parameter.channelTypes != null) {
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

                if (parameter.hasAutocomplete()) {
                    requireUser(optionType.canSupportChoices()) {
                        "Slash command parameter #$i does not support autocomplete"
                    }

                    data.isAutoComplete = true
                }

                if (optionType.canSupportChoices()) {
                    var choices: Collection<Command.Choice>? = null

                    if (!parameter.choices.isNullOrEmpty()) {
                        choices = parameter.choices
                    } else {
                        val predefinedChoices = resolver.getPredefinedChoices(guild)
                        if (predefinedChoices.isNotEmpty()) {
                            choices = predefinedChoices
                        }
                    }

                    if (choices != null) {
                        requireUser(!parameter.hasAutocomplete()) {
                            "Slash command parameter #$i cannot have autocomplete and choices at the same time"
                        }

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

    internal inline fun <reified T> ApplicationCommandInfo.checkEventScope() {
        val firstParamKlass = method.valueParameters.first().type.jvmErasure
        if (scope.isGuildOnly) {
            if (!firstParamKlass.isSubclassOf(T::class)) {
                Logging.getLogger().warn("${method.shortSignature} : First parameter could be a ${T::class.simpleName} as to benefit from non-null getters")
            }
        } else if (firstParamKlass.isSubclassOf(T::class)) {
            throwUser("Cannot use ${T::class.simpleName} on a global application command")
        }
    }

    internal fun String.toVarArgName(varArgNum: Int) = when (varArgNum) {
        0 -> this
        else -> "${this}_$varArgNum"
    }
}