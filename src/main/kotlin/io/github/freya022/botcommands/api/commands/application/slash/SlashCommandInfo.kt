package io.github.freya022.botcommands.api.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandParameter

interface SlashCommandInfo : ApplicationCommandInfo {
    val description: String

    override val parameters: List<SlashCommandParameter>
}