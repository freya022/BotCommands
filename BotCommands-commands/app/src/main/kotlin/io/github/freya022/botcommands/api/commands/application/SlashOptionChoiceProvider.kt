package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command

/**
 * Provider of choices for [options][SlashOption] of **annotated** slash commands.
 *
 * If you wish to add choices to a command's option, you must implement this on the same declaring class.
 */
interface SlashOptionChoiceProvider {
    /**
     * Returns the choices available for this command path,
     * on the specific [optionName].
     *
     * The choices returned by this method will have their name localized
     * if they are present in the [localization bundles][BApplicationConfigBuilder.addLocalizations].
     *
     * @param guild       The [Guild] in which the command is, might be `null` for global commands with choices
     * @param commandPath The [CommandPath] of the command, this is composed of it's name and optionally of its group and subcommand name
     * @param optionName  The option name, not the same as the parameter name, this is the same name that appears on Discord
     *
     * @return The list of choices for this slash command's options
     *
     * @see SlashParameterResolver.getPredefinedChoices
     *
     * @see SlashCommandOptionBuilder.choices DSL equivalent
     */
    fun getOptionChoices(guild: Guild?, commandPath: CommandPath, optionName: String): List<Command.Choice>
}
