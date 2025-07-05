package io.github.freya022.botcommands.api.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.application.builder.TopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashCommandOptionAggregateBuilder
import net.dv8tion.jda.api.interactions.commands.localization.LocalizationFunction
import kotlin.reflect.KFunction

interface TopLevelSlashCommandBuilder : SlashCommandBuilder,
                                        TopLevelApplicationCommandBuilder<SlashCommandOptionAggregateBuilder> {
    /**
     * Adds a subcommand, **must not contain any spaces and no upper cases**.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped.
     *
     * @see JDASlashCommand.subcommand
     */
    fun subcommand(name: String, function: KFunction<Any>, block: SlashSubcommandBuilder.() -> Unit = {})

    /**
     * Adds a subcommand group, **must not contain any spaces and no upper cases**.
     *
     * This can be localized, see [LocalizationFunction] on how commands are mapped.
     *
     * @see LocalizationFunction
     *
     * @see JDASlashCommand.group
     */
    fun subcommandGroup(name: String, block: SlashSubcommandGroupBuilder.() -> Unit)
}
