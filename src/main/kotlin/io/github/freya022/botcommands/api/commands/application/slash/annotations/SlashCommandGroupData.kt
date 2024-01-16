package io.github.freya022.botcommands.api.commands.application.slash.annotations

import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashSubcommandGroupBuilder
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction

/**
 * Additional annotation for slash command groups.
 *
 * This is only used to specify properties on the subcommand group of the annotated slash command,
 * such as the description.
 *
 * This must be used on a [subcommand][JDASlashCommand.subcommand] in a [subcommand group][JDASlashCommand.group],
 * and specified at most once per slash command group,
 * e.g., if you have `/ban temp user` and `/ban temp recent`, you can annotate at most one of them.
 *
 * @see JDASlashCommand @JDASlashCommand
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SlashCommandGroupData(
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
     * @see SlashSubcommandGroupBuilder.description DSL equivalent
     */
    val description: String = ""
)
