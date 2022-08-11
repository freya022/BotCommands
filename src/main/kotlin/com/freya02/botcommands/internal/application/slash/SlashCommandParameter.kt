package com.freya02.botcommands.internal.application.slash

import com.freya02.botcommands.annotations.api.application.slash.annotations.ChannelTypes
import com.freya02.botcommands.api.application.ValueRange
import com.freya02.botcommands.api.application.builder.OptionBuilder
import com.freya02.botcommands.api.application.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.application.slash.autocomplete.AutocompleteHandler
import com.freya02.botcommands.internal.enumSetOf
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.interactions.commands.Command
import java.util.*
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

class SlashCommandParameter(
    slashCommandInfo: SlashCommandInfo,
    slashCmdOptionBuilders: Map<String, OptionBuilder>,
    parameter: KParameter,
    optionBuilder: SlashCommandOptionBuilder,
    resolver: SlashParameterResolver
) : AbstractSlashCommandParameter(
    parameter, optionBuilder, resolver
) {
    val description: String = optionBuilder.description
    override val isOptional: Boolean
    internal val autocompleteHandler: AutocompleteHandler? = when {
        optionBuilder.autocompleteInfo != null -> AutocompleteHandler(
            slashCommandInfo,
            slashCmdOptionBuilders,
            optionBuilder.autocompleteInfo!!
        )
        else -> null
    }

    val choices: List<Command.Choice>? = optionBuilder.choices
    val range: ValueRange? = optionBuilder.valueRange

    val channelTypes: EnumSet<ChannelType>?

    init {
        val optionOptional = optionBuilder.optional
        this.isOptional = when {
            optionOptional != null -> {
                if (optionOptional != kParameter.isNullable) {
                    throwUser("Option '${optionBuilder.name}' does not have the same nullability as it's function parameter")
                }

                optionOptional
            }
            else -> kParameter.isNullable
        }

        val channelTypesAnnotation = kParameter.findAnnotation<ChannelTypes>()
        if (optionBuilder.channelTypes != null && channelTypesAnnotation != null) {
            throwUser("Parameter $kParameter has channel types specified in both the annotation and the DSL builder, consider only use one of them")
        }

        this.channelTypes = when {
            channelTypesAnnotation != null -> enumSetOf(*channelTypesAnnotation.value)
            optionBuilder.channelTypes != null -> optionBuilder.channelTypes
            else -> enumSetOf()
        }
    }

    fun hasAutocomplete() = autocompleteHandler != null
}