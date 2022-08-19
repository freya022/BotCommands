package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.application.ValueRange
import com.freya02.botcommands.api.commands.application.builder.OptionBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.commands.application.slash.autocomplete.AutocompleteHandler
import com.freya02.botcommands.internal.enumSetOf
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.interactions.commands.Command
import java.util.*
import kotlin.reflect.KParameter

class SlashCommandParameter(
    slashCommandInfo: SlashCommandInfo,
    slashCmdOptionBuilders: Map<String, OptionBuilder>,
    parameter: KParameter,
    optionBuilder: SlashCommandOptionBuilder,
    resolver: SlashParameterResolver<*, *>
) : AbstractSlashCommandParameter(
    parameter, optionBuilder, resolver
) {
    val description: String = optionBuilder.description

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
        this.channelTypes = optionBuilder.channelTypes ?: enumSetOf()
    }

    fun hasAutocomplete() = autocompleteHandler != null
}