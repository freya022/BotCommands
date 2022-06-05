package com.freya02.botcommands.api.application.builder

import net.dv8tion.jda.api.interactions.commands.Command.Choice

class SlashCommandOptionBuilder(name: String): ApplicationCommandOptionBuilder(name) {
    var description: String = "No description"
    var optional: Boolean? = null
    var choices: List<Choice>? = null
}
