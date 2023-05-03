package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.Logging
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.GlobalSlashEvent
import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.internal.commands.application.ApplicationGeneratedMethodParameter
import com.freya02.botcommands.internal.core.options.OptionType
import com.freya02.botcommands.internal.parameters.resolvers.channels.ChannelResolver
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import net.dv8tion.jda.api.interactions.commands.OptionType as JDAOptionType

internal object SlashUtils {
    val fakeSlashFunction = ::fakeFunction

    @Suppress("UNUSED_PARAMETER")
    private fun fakeFunction(event: GlobalSlashEvent): Nothing = throwInternal("Fake function was used")

    fun IExecutableInteractionInfo.checkDefaultValue(
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

        val options = parameters.flatMap { it.commandOptions }
        //Move all optional options at the front
        val orderedOptions = options.sortedWith { o1, o2 ->
            when {
                //Optional options are placed after required options
                o1.isOptional && !o2.isOptional -> 1
                //Required options are placed before optional options
                !o1.isOptional && o2.isOptional -> -1
                //If both are optional/required, keep order using index
                else -> options.indexOf(o1).compareTo(options.indexOf(o2))
            }
        }
        for (option in orderedOptions) {
            if (option.optionType != OptionType.OPTION) continue

            option as SlashCommandOption

            val name = option.discordName
            val description = option.description

            val resolver = option.resolver
            val optionType = resolver.optionType

            val data = OptionData(optionType, name, description)

            when (optionType) {
                JDAOptionType.CHANNEL -> {
                    //If there are no specified channel types, then try to get the channel type from AbstractChannelResolver
                    // Otherwise set the channel types of the option, if available
                    if (option.channelTypes.isEmpty() && resolver is ChannelResolver) {
                        data.setChannelTypes(resolver.channelTypes)
                    } else if (option.channelTypes.isEmpty()) {
                        data.setChannelTypes(option.channelTypes)
                    }
                }
                JDAOptionType.INTEGER -> {
                    option.range?.let {
                        data.setMinValue(it.min.toLong())
                        data.setMaxValue(it.max.toLong())
                    }
                }
                JDAOptionType.NUMBER -> {
                    option.range?.let {
                        data.setMinValue(it.min.toDouble())
                        data.setMaxValue(it.max.toDouble())
                    }
                }
                JDAOptionType.STRING -> {
                    option.length?.let {
                        data.setRequiredLength(it.min, it.max)
                    }
                }
                else -> {}
            }

            if (option.hasAutocomplete()) {
                requireUser(optionType.canSupportChoices(), option.kParameter.function) {
                    "Slash command option #${option.index} does not support autocomplete"
                }

                data.isAutoComplete = true
            }

            if (optionType.canSupportChoices()) {
                var choices: Collection<Command.Choice>? = null

                if (!option.choices.isNullOrEmpty()) {
                    choices = option.choices
                } else if (option.usePredefinedChoices) { //Opt in
                    val predefinedChoices = resolver.getPredefinedChoices(guild)
                    if (predefinedChoices.isEmpty())
                        throwUser(option.kParameter.function, "Predefined choices were used for option '${option.declaredName}' but no choices were returned")
                    choices = predefinedChoices
                }

                if (choices != null) {
                    requireUser(!option.hasAutocomplete(), option.kParameter.function) {
                        "Slash command option #${option.index} cannot have autocomplete and choices at the same time"
                    }

                    data.addChoices(choices)
                }
            }

            data.isRequired = !option.isOptional

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
