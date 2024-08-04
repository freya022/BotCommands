package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo
import io.github.freya022.botcommands.api.core.IDeclarationSiteHolder
import io.github.freya022.botcommands.api.core.utils.shortQualifiedName
import io.github.freya022.botcommands.internal.commands.application.slash.options.SlashCommandOptionImpl
import io.github.freya022.botcommands.internal.core.options.AbstractGeneratedOption
import io.github.freya022.botcommands.internal.parameters.resolvers.IChannelResolver
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.classRef
import io.github.freya022.botcommands.internal.utils.requireAt
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure
import net.dv8tion.jda.api.interactions.commands.OptionType as JDAOptionType

internal object SlashUtils {
    private val logger = KotlinLogging.logger { }

    internal val fakeSlashFunction = SlashUtils::fakeFunction.reflectReference()

    @Suppress("UNUSED_PARAMETER", "MemberVisibilityCanBePrivate")
    internal fun fakeFunction(event: GlobalSlashEvent): Nothing = throwInternal("Fake function was used")

    internal fun KFunction<*>.isFakeSlashFunction() = this === fakeSlashFunction

    context(IDeclarationSiteHolder)
    internal inline fun <T : AbstractGeneratedOption> T.getCheckedDefaultValue(supplier: (T) -> Any?): Any? = let { option ->
        return supplier(this).also { defaultValue ->
            checkDefaultValue(option, defaultValue)
        }
    }

    private fun IDeclarationSiteHolder.checkDefaultValue(option: AbstractGeneratedOption, defaultValue: Any?) {
        if (defaultValue != null) {
            val expectedType: KClass<*> = option.type.jvmErasure
            requireAt(expectedType.isInstance(defaultValue), declarationSite) {
                "Generated value supplier for parameter #${option.index} has returned a default value of type ${defaultValue::class.simpleName} but a value of type ${expectedType.simpleName} was expected"
            }
        } else {
            requireAt(option.isOptionalOrNullable, declarationSite) {
                "Generated value supplier for parameter #${option.index} has returned a null value but parameter is not optional"
            }
        }
    }

    internal fun SlashCommandInfo.getDiscordOptions(guild: Guild?) = parameters
        .flatMap { it.allOptions }
        .filterIsInstance<SlashCommandOptionImpl>()
        //Move all optional options at the front
        .let { options ->
            options.sortedWith { o1, o2 ->
                when {
                    //Optional options are placed after required options
                    o1.isOptionalOrNullable && o2.isRequired -> 1
                    //Required options are placed before optional options
                    o1.isRequired && o2.isOptionalOrNullable -> -1
                    //If both are optional/required, keep order using index
                    else -> options.indexOf(o1).compareTo(options.indexOf(o2))
                }
            }
        }
        .map { option ->
            OptionData(option.resolver.optionType, option.discordName, option.description)
                .also { configureOptionData(option, it, guild) }
        }

    private fun configureOptionData(option: SlashCommandOptionImpl, data: OptionData, guild: Guild?) {
        val resolver = option.resolver
        val optionType = resolver.optionType

        when (optionType) {
            JDAOptionType.CHANNEL -> {
                //If there are no specified channel types, then try to get the channel type from AbstractChannelResolver
                // Otherwise set the channel types of the option, if available
                if (resolver is IChannelResolver) {
                    data.setChannelTypes(resolver.channelTypes)
                } else {
                    logger.warn { "Encountered a CHANNEL slash command option, but the resolver (${resolver.javaClass.shortQualifiedName}) does not implement ${classRef<IChannelResolver>()}" }
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
            requireAt(optionType.canSupportChoices(), option.typeCheckingFunction) {
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
                requireAt(predefinedChoices.isNotEmpty(), option.typeCheckingFunction) {
                    "Predefined choices were used for option '${option.declaredName}' but no choices were returned"
                }
                choices = predefinedChoices
            }

            if (choices != null) {
                requireAt(!option.hasAutocomplete(), option.typeCheckingFunction) {
                    "Slash command option #${option.index} cannot have autocomplete and choices at the same time"
                }

                data.addChoices(choices)
            }
        }

        data.isRequired = option.isRequired
    }
}
