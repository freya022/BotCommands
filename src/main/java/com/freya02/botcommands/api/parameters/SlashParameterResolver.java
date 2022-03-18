package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.api.application.CommandPath;
import com.freya02.botcommands.api.application.GuildApplicationSettings;
import com.freya02.botcommands.internal.application.slash.SlashCommandInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Interface which indicates this class can resolve parameters for application commands
 */
public interface SlashParameterResolver {
	/**
	 * Returns the supported {@link OptionType} for this slash command parameter
	 *
	 * @return The supported {@link OptionType} for this slash command parameter
	 */
	@NotNull
	OptionType getOptionType();

	/**
	 * Returns a constant list of {@link Choice choices} for this slash parameter resolver
	 * This will be applied to all command parameters of this type, but can still be overridden if there are choices set in {@link GuildApplicationSettings#getOptionChoices(Guild, CommandPath, int)}
	 * <br>This could be useful for, say, an enum resolver, or anything where the choices do not change between commands
	 */
	@NotNull
	default Collection<Command.Choice> getPredefinedChoices(@Nullable Guild guild) {
		return Collections.emptyList();
	}

	/**
	 * Returns a resolved object for this {@link OptionMapping}
	 *
	 * @param context       The {@link BContext} of this bot
	 * @param info          The slash command info of the command being executed
	 * @param event         The event of this interaction, could be a {@link SlashCommandInteractionEvent} or a {@link CommandAutoCompleteInteractionEvent}
	 * @param optionMapping The {@link OptionMapping} to be resolved
	 * @return The resolved option mapping
	 */
	@Nullable
	Object resolve(@NotNull BContext context, @NotNull SlashCommandInfo info, @NotNull CommandInteractionPayload event, @NotNull OptionMapping optionMapping);
}