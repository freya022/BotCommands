package com.freya02.botcommands.api.application;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.SettingsProvider;
import com.freya02.botcommands.api.annotations.CommandId;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.DefaultValueSupplier;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.slash.annotations.VarArgs;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Interface providing getters for settings commands stuff on a per-guild basis
 *
 * <h2>Implementation note:</h2>
 * These settings are looked first in {@link ApplicationCommand} and then again in {@link SettingsProvider}
 * <br>This provides the user either a clean enough look in SettingsProvider (no boilerplate in every SlashCommand) or an easy-to-use method in {@link ApplicationCommand}(s)
 */
public interface GuildApplicationSettings {
	/**
	 * Returns the choices available for this command path, on the specific <code>optionIndex</code> (option index starts at 0 and is composed of only the parameters annotated with {@link AppOption @AppOption})
	 * <p>
	 * <br><i>The choices returned by this method will have their name and values localized if they are present in the BotCommands resource bundles</i>
	 *
	 * @param guild       The {@link Guild} in which the command is, might be <code>null</code> for global commands with choices
	 * @param commandPath The {@link CommandPath} of the command, this is composed of it's name and optionally of its group and subcommand name
	 * @param optionIndex The index of the option, this starts at 0 and goes to how many {@link AppOption @AppOption} there are, minus 1
	 *
	 * @return The list of choices for this slash command's options
	 *
	 * @see SlashParameterResolver#getPredefinedChoices()
	 */
	@NotNull
	default List<Command.Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, int optionIndex) {
		return Collections.emptyList();
	}

	/**
	 * Returns the list of {@linkplain CommandPrivilege command privileges} for the given <b>base command name (most left name), no group, no subcommand</b>
	 *
	 * @param cmdBaseName Base name (top level) of the command to get the permissions of
	 * @param guild       The guild of the command
	 *
	 * @return An empty Collection if the permissions should be cleared, or the privileges to apply to it.
	 */
	@NotNull
	default List<CommandPrivilege> getCommandPrivileges(@NotNull Guild guild, @NotNull String cmdBaseName) {
		return Collections.emptyList();
	}

	/**
	 * Returns a collection of {@link Guild} IDs in which the specified command ID will be allowed to be pushed in
	 * <br>A <code>null</code> return value means that the command can be used in any guild
	 * <br>Meanwhile, an empty list means that the command cannot be used anywhere
	 *
	 * <p>You will have exceptions later if multiple commands IDs under the same command path share at least one guild ID
	 *
	 * <p>Be very cautious with your command IDs.
	 *
	 * @param context     The current BotCommands context
	 * @param commandId   The ID of the command that has been set with {@link CommandId}
	 * @param commandPath The {@link CommandPath} of the specified command ID
	 *
	 * @return A collection of Guild IDs where the specified command is allowed to be pushed in
	 * 		<br>This returns <code>null</code> by default
	 */
	@Nullable
	default Collection<Long> getGuildsForCommandId(@NotNull BContext context, @Nullable String commandId, @NotNull CommandPath commandPath) {
		return null;
	}

	/**
	 * Returns the default value supplier of an {@link AppOption}, for slash commands only
	 * <br>This method is called only if your option is annotated
	 * <p>This method will only be called once per command option per guild
	 *
	 * @param context       The current BotCommands context
	 * @param guild         The {@link Guild} in which to add the default value
	 * @param commandId     The ID of the command, as optionally set in {@link CommandId}, might be <code>null</code>
	 * @param commandPath   The path of the command, as set in {@link JDASlashCommand}
	 * @param optionName    The name of the <b>transformed</b> command option, might not be equal to the parameter name
	 * @param parameterType The <b>boxed</b> type of the command option, or the item type of the parameter is annotated with {@link VarArgs}
	 *
	 * @return A {@link DefaultValueSupplier} if the option can be substituted with an object
	 */
	@Nullable
	default DefaultValueSupplier getDefaultValueSupplier(@NotNull BContext context, @NotNull Guild guild,
	                                                     @Nullable String commandId, @NotNull CommandPath commandPath,
	                                                     @NotNull String optionName, @NotNull Class<?> parameterType) {
		return null;
	}
}
