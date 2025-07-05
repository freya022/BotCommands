package io.github.freya022.botcommands.api.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashCommandGroupData
import io.github.freya022.botcommands.api.core.IDeclarationSiteHolderBuilder
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.internal.commands.CommandDSL
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import kotlin.reflect.KFunction

@CommandDSL
interface SlashSubcommandGroupBuilder : INamedCommand, IDeclarationSiteHolderBuilder {
    //TODO change docs when Discord eventually decides to not have a mess of a command list
    /**
     * Short description of the subcommand group.
     * May be displayed on Discord.
     *
     * If this description is omitted, a default localization is
     * searched in [the command localization bundles][BApplicationConfigBuilder.addLocalizations]
     * using the root locale, for example: `MyCommands.json`.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped,
     * for example, on `/ban temp recent` => `ban.temp.description`.
     *
     * @see LocalizationFunction
     *
     * @see SlashCommandGroupData.description
     */
    var description: String?

    /**
     * Adds a subcommand, **must not contain any spaces and no upper cases**.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped.
     *
     * @see JDASlashCommand.subcommand
     */
    fun subcommand(name: String, function: KFunction<Any>, block: SlashSubcommandBuilder.() -> Unit = {})
}
