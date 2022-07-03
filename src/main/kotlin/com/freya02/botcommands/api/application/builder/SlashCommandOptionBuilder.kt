package com.freya02.botcommands.api.application.builder

import com.freya02.botcommands.api.application.AutocompleteInfo
import com.freya02.botcommands.api.application.ValueRange
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import java.util.*

class SlashCommandOptionBuilder(name: String): ApplicationCommandOptionBuilder(name) {
    var description: String = "No description"
    var optional: Boolean? = null
    var choices: List<Choice>? = null

    var valueRange: ValueRange? = null
    var channelTypes: EnumSet<ChannelType>? = null

    var autocompleteInfo: AutocompleteInfo? = null
        private set

    fun autocomplete(block: AutocompleteInfoBuilder.() -> Unit) {
        autocompleteInfo = AutocompleteInfoBuilder().apply(block).build()
    }
}
