package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.internal.commands.application.ApplicationGeneratedMethodParameter
import com.freya02.botcommands.internal.parameters.MethodParameterType
import com.freya02.botcommands.internal.parameters.resolvers.channels.ChannelResolver
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

internal object SlashUtils {
    fun IExecutableInteractionInfo.checkDefaultValue(
        parameter: ApplicationGeneratedMethodParameter,
        defaultValue: Any?
    ) {
        requireUser(defaultValue != null || parameter.isOptional) {
            "Generated value supplier for parameter #${parameter.index} has returned a null value but parameter is not optional"
        }

        if (defaultValue == null) return

        val expectedType: KClass<*> = parameter.kParameter.type.jvmErasure

        requireUser(expectedType.isSuperclassOf(defaultValue::class)) {
            "Generated value supplier for parameter #${parameter.index} has returned a default value of type ${defaultValue::class.simpleName} but a value of type ${expectedType.simpleName} was expected"
        }
    }

    @JvmStatic
    fun SlashCommandInfo.getMethodOptions(guild: Guild?): List<OptionData> {
        val list: MutableList<OptionData> = ArrayList()

		for (parameter in parameters.flatMap { it.commandOptions }) {
            if (parameter.methodParameterType != MethodParameterType.OPTION) continue

            parameter as SlashCommandOption

            val name = parameter.discordName
            val description = parameter.description

            val resolver = parameter.resolver
            val optionType = resolver.optionType

            val data = OptionData(optionType, name, description)

            when (optionType) {
                OptionType.CHANNEL -> {
                    //If there are no specified channel types, then try to get the channel type from AbstractChannelResolver
                    // Otherwise set the channel types of the parameter, if available
                    if (parameter.channelTypes.isEmpty() && resolver is ChannelResolver) {
                        data.setChannelTypes(resolver.channelTypes)
                    } else if (parameter.channelTypes.isEmpty()) {
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
                OptionType.STRING -> {
                    parameter.length?.let {
                        data.setRequiredLength(it.min, it.max)
                    }
                }
                else -> {}
            }

            if (parameter.hasAutocomplete()) {
                requireUser(optionType.canSupportChoices(), parameter.kParameter.function) {
                    "Slash command parameter #${parameter.index} does not support autocomplete"
                }

                data.isAutoComplete = true
            }

            if (optionType.canSupportChoices()) {
                var choices: Collection<Command.Choice>? = null

                if (!parameter.choices.isNullOrEmpty()) {
                    choices = parameter.choices
                } else if (parameter.usePredefinedChoices) { //Opt in
                    val predefinedChoices = resolver.getPredefinedChoices(guild)
                    if (predefinedChoices.isEmpty())
                        throwUser(parameter.kParameter.function, "Predefined choices were used for parameter '${parameter.declaredName}' but no choices were returned")
                    choices = predefinedChoices
                }

                if (choices != null) {
                    requireUser(!parameter.hasAutocomplete(), parameter.kParameter.function) {
                        "Slash command parameter #${parameter.index} cannot have autocomplete and choices at the same time"
                    }

                    data.addChoices(choices)
                }
            }

            //TODO this might not be true for varargs
            data.isRequired = !parameter.isOptional

            list.add(data)
        }

        return list
    }

    internal inline fun <reified T> ApplicationCommandBuilder<*>.checkEventScope() {
        val firstParamKlass = function.valueParameters.first().type.jvmErasure
        if (topLevelBuilder.scope.isGuildOnly) {
            if (!firstParamKlass.isSubclassOf(T::class)) {
                Logging.getLogger().warn("${function.shortSignature} : First parameter could be a ${T::class.simpleName} as to benefit from non-null getters")
            }
        } else if (firstParamKlass.isSubclassOf(T::class)) {
            throwUser("Cannot use ${T::class.simpleName} on a global application command")
        }
    }
}
