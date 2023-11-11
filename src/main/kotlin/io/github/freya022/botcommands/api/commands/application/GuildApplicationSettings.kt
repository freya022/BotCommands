package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.application.annotations.CommandId
import io.github.freya022.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.core.config.BApplicationConfigBuilder
import io.github.freya022.botcommands.api.core.reflect.ParameterType
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.interactions.commands.Command

/**
 * Interface providing guild-specific settings (enabled, choices, generated values, etc...)
 *
 * **Only applies to annotated commands**
 *
 * @see ApplicationCommand
 */
interface GuildApplicationSettings {
    /**
     * Returns the choices available for this command path,
     * on the specific `optionIndex` (option index starts at 0 and are composed of only the parameters annotated with [@SlashOption][SlashOption]).
     *
     * The choices returned by this method will have their name localized
     * if they are present in the [localization bundles][BApplicationConfigBuilder.addLocalizations].
     *
     * @param guild       The [Guild] in which the command is, might be `null` for global commands with choices
     * @param commandPath The [CommandPath] of the command, this is composed of it's name and optionally of its group and subcommand name
     * @param optionName  The option name, this is the same name that appears on Discord
     *
     * @return The list of choices for this slash command's options
     *
     * @see SlashParameterResolver.getPredefinedChoices
     *
     * @see SlashCommandOptionBuilder.choices DSL equivalent
     */
    fun getOptionChoices(guild: Guild?, commandPath: CommandPath, optionName: String): List<Command.Choice> {
        return emptyList()
    }

    /**
     * Returns a collection of [Guild] IDs in which the specified command ID will be allowed to be pushed in.
     *
     * Return values:
     * - `null` if the command can be used in any guild
     * - An empty list if the command cannot be used anywhere
     * - A list of guild IDs, where the command will be usable
     *
     * Make sure to not allow more than one command with the same path.
     *
     * @param commandId   The ID of the command that has been set with [@CommandId][CommandId]
     * @param commandPath The [CommandPath] of the specified command ID
     *
     * @return A collection of Guild IDs where the specified command is allowed to be pushed in<br>
     *         This returns `null` by default
     *
     * @see CommandId @CommandId
     */
    fun getGuildsForCommandId(commandId: String, commandPath: CommandPath): Collection<Long>? {
        return null
    }

    /**
     * Returns the generated value supplier of a [@GeneratedOption][GeneratedOption].
     *
     * This function will only be called once per command option per guild.
     *
     * @param guild         The [Guild] in which to add the default value, `null` if the scope is **not** [CommandScope.GUILD]
     * @param commandId     The ID of the command, as optionally set in [@CommandId][CommandId], might be `null`
     * @param commandPath   The path of the command, as set in [@JDASlashCommand][JDASlashCommand]
     * @param optionName    The name of the **transformed** command option,
     *                      which might not be equal to the parameter name
     * @param parameterType The **boxed** type of the command option
     *
     * @return A [ApplicationGeneratedValueSupplier] to generate the option on command execution
     */
    fun getGeneratedValueSupplier(
        guild: Guild?,
        commandId: String?, commandPath: CommandPath,
        optionName: String, parameterType: ParameterType
    ): ApplicationGeneratedValueSupplier {
        val errorStr = buildString {
            append("Option '$optionName' in command path '${commandPath.fullPath}'")
            if (commandId != null) append(" (id '$commandId')")
            if (guild != null) append(" in guild '${guild.name}' (id ${guild.id})")
            append(" is a generated option but no generated value supplier has been given")
        }

        throw IllegalArgumentException(errorStr)
    }
}
