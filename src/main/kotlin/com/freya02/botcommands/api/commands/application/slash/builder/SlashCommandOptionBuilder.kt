package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.commands.application.ValueRange
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionBuilder
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteInfo
import com.freya02.botcommands.api.commands.application.slash.autocomplete.builder.AutocompleteInfoBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.application.autocomplete.AutocompleteInfoContainer
import com.freya02.botcommands.internal.throwUser
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.interactions.commands.Command.Choice
import java.util.*

class SlashCommandOptionBuilder(private val context: BContextImpl, declaredName: String, optionName: String): ApplicationCommandOptionBuilder(declaredName, optionName) {
    var description: String = "No description"
    var choices: List<Choice>? = null

    var valueRange: ValueRange? = null
    var channelTypes: EnumSet<ChannelType>? = null

    var autocompleteInfo: AutocompleteInfo? = null
        private set

    /**
     * Name must be unique
     *
     * Recommended naming: `ClassSimpleName: AutocompletedField`
     *
     * Example: `SlashTag: tagName`
     */
    fun autocomplete(name: String, block: AutocompleteInfoBuilder.() -> Unit) {
        autocompleteInfo = AutocompleteInfoBuilder(name).apply(block).build()
    }

    fun autocompleteReference(name: String) {
        autocompleteInfo = context.getService<AutocompleteInfoContainer>()[name] ?: throwUser("Unknown autocomplete handler: $name")
    }
}
