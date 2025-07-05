package io.github.freya022.botcommands.api.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashOptionRegistry
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction

interface SlashCommandBuilder : ApplicationCommandBuilder<SlashCommandOptionAggregateBuilder>,
                                SlashOptionRegistry {
    /**
     * Short description of the command displayed on Discord.
     *
     * If this description is omitted, a default localization is
     * searched in [the command localization bundles][BApplicationConfigBuilder.addLocalizations]
     * using the root locale, for example: `MyCommands.json`.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped, example: `ban.description`.
     *
     * @see LocalizationFunction
     *
     * @see JDASlashCommand.description
     */
    var description: String?
}
