package io.github.freya022.botcommands.api.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandOption
import io.github.freya022.botcommands.api.commands.application.LengthRange
import io.github.freya022.botcommands.api.commands.application.ValueRange
import net.dv8tion.jda.api.interactions.commands.Command

interface SlashCommandOption : ApplicationCommandOption {
    val discordName: String
    val description: String
    val usePredefinedChoices: Boolean
    val choices: List<Command.Choice>?
    val range: ValueRange?
    val length: LengthRange?

    fun hasAutocomplete(): Boolean
}