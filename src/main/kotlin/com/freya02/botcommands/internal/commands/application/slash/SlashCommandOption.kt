package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.application.LengthRange
import com.freya02.botcommands.api.commands.application.ValueRange
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.commands.application.slash.autocomplete.AutocompleteHandler
import com.freya02.botcommands.internal.enumSetOf
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*

class SlashCommandOption(
    slashCommandInfo: SlashCommandInfo,
    optionAggregateBuilders: Map<String, OptionAggregateBuilder>,
    optionBuilder: SlashCommandOptionBuilder,
    resolver: SlashParameterResolver<*, *>
) : AbstractSlashCommandOption(optionBuilder, resolver) {
    val description: String = optionBuilder.description

    internal val autocompleteHandler: AutocompleteHandler? = when {
        optionBuilder.autocompleteInfo != null -> AutocompleteHandler(
            slashCommandInfo,
            optionAggregateBuilders,
            optionBuilder.autocompleteInfo!!
        )
        else -> null
    }

    val usePredefinedChoices = optionBuilder.usePredefinedChoices
    val choices: List<Command.Choice>? = optionBuilder.choices
    val range: ValueRange? = optionBuilder.valueRange
    val length: LengthRange? = optionBuilder.lengthRange

    val channelTypes: EnumSet<ChannelType> = optionBuilder.channelTypes ?: enumSetOf()

    init {
        if (range != null) {
            if (resolver.optionType != OptionType.NUMBER && resolver.optionType != OptionType.INTEGER) {
                throw IllegalStateException("Cannot use ranges on an option that doesn't accept an integer/number")
            }
        } else if (length != null) {
            if (resolver.optionType != OptionType.STRING) {
                throw IllegalStateException("Cannot use lengths on an option that doesn't accept an string")
            }
        }
    }

    fun hasAutocomplete() = autocompleteHandler != null
}