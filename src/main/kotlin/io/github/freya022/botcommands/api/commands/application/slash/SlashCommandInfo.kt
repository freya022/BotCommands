package io.github.freya022.botcommands.api.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo

interface SlashCommandInfo : ApplicationCommandInfo {
    val description: String

    override val parameters: List<SlashCommandParameter>

    override val discordOptions: List<SlashCommandOption>
        get() = parameters.flatMap { it.allOptions }.filterIsInstance<SlashCommandOption>()

    val asMention: String get() = "</$fullCommandName:${topLevelInstance.id}>"
}