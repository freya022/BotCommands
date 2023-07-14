package com.freya02.botcommands.api.commands.application;

import com.freya02.botcommands.api.commands.CommandPath;
import com.freya02.botcommands.api.commands.annotations.GeneratedOption;
import com.freya02.botcommands.api.commands.application.annotations.CommandId;
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption;
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder;
import com.freya02.botcommands.api.parameters.ParameterType;
import com.freya02.botcommands.api.parameters.SlashParameterResolver;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Interface providing getters for settings commands stuff on a per-guild basis
 */
public interface GuildApplicationSettings {
	/**
	 * Returns the choices available for this command path, on the specific {@code optionIndex} (option index starts at 0 and is composed of only the parameters annotated with {@link SlashOption @SlashOption})
	 *
	 * <p><i>The choices returned by this method will have their name and values localized if they are present in the BotCommands resource bundles</i>
	 *
	 * <p><b>Note:</b> This method is only used for annotation-declared application commands.
	 *
	 * @param guild       The {@link Guild} in which the command is, might be {@code null} for global commands with choices
	 * @param commandPath The {@link CommandPath} of the command, this is composed of it's name and optionally of its group and subcommand name
	 * @param optionName  The option name, this is the same name that appears on Discord
	 *
	 * @return The list of choices for this slash command's options
	 *
	 * @see SlashParameterResolver#getPredefinedChoices(Guild)
	 *
	 * @see SlashCommandOptionBuilder#setChoices(List) DSL equivalent
	 */
	@NotNull
	default List<Command.Choice> getOptionChoices(@Nullable Guild guild, @NotNull CommandPath commandPath, @NotNull String optionName) {
		return Collections.emptyList();
	}

	/**
	 * Returns a collection of {@link Guild} IDs in which the specified command ID will be allowed to be pushed in
	 * <br>A {@code null} return value means that the command can be used in any guild
	 * <br>Meanwhile, an empty list means that the command cannot be used anywhere
	 *
	 * <p>You will have exceptions later if multiple commands IDs under the same command path share at least one guild ID
	 *
	 * <p>Be very cautious with your command IDs.
	 *
	 * @param commandId   The ID of the command that has been set with {@link CommandId}
	 * @param commandPath The {@link CommandPath} of the specified command ID
	 *
	 * @return A collection of Guild IDs where the specified command is allowed to be pushed in
	 * 	   <br>This returns {@code null} by default
	 *
	 * @see CommandId
	 */
	@Nullable
	default Collection<Long> getGuildsForCommandId(@NotNull String commandId, @NotNull CommandPath commandPath) {
		return null;
	}

	/**
	 * Returns the generated value supplier of an {@link GeneratedOption}, if the method doesn't return a generated value supplier, the framework will throw.
	 * <br>This method is called only if your option is annotated with {@link GeneratedOption}
	 *
	 * <p>This method will only be called once per command option per guild
	 *
	 * @param guild         The {@link Guild} in which to add the default value, {@code null} if the scope is <b>not</b> {@link CommandScope#GUILD}
	 * @param commandId     The ID of the command, as optionally set in {@link CommandId}, might be {@code null}
	 * @param commandPath   The path of the command, as set in {@link JDASlashCommand}
	 * @param optionName    The name of the <b>transformed</b> command option, might not be equal to the parameter name
	 * @param parameterType The <b>boxed</b> type of the command option
	 *
	 * @return A {@link ApplicationGeneratedValueSupplier} to generate the option on command execution
	 */
	@NotNull
	default ApplicationGeneratedValueSupplier getGeneratedValueSupplier(@Nullable Guild guild,
	                                                                    @Nullable String commandId, @NotNull CommandPath commandPath,
	                                                                    @NotNull String optionName, @NotNull ParameterType parameterType) {
		final StringBuilder errorBuilder = new StringBuilder("Option '%s' in command path '%s'".formatted(optionName, commandPath.getFullPath()));
		if (commandId != null) errorBuilder.append(" (id '%s')".formatted(commandId));
		if (guild != null) errorBuilder.append(" in guild '%s' (id %s)".formatted(guild.getName(), guild.getId()));
		errorBuilder.append(" is a generated option but no generated value supplier has been given");

		throw new IllegalArgumentException(errorBuilder.toString());
	}
}
