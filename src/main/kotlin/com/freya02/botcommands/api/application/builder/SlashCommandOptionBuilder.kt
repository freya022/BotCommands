package com.freya02.botcommands.api.application.builder

import com.freya02.botcommands.api.application.AutocompleteInfo
import com.freya02.botcommands.api.application.ValueRange
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import java.util.*

class SlashCommandOptionBuilder(declaredName: String, optionName: String): ApplicationCommandOptionBuilder(declaredName, optionName) {
    var description: String = "No description"
    var choices: List<Choice>? = null

    var valueRange: ValueRange? = null
    var channelTypes: EnumSet<ChannelType>? = null

    var autocompleteInfo: AutocompleteInfo? = null
        private set

    fun autocomplete(block: AutocompleteInfoBuilder.() -> Unit) {
        autocompleteInfo = AutocompleteInfoBuilder().apply(block).build()
    }
}
