package com.freya02.botcommands.api.parameters;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

/**
 * Interface which indicates this class can resolve parameters for application commands
 */
public interface SlashParameterResolver {
	/**
	 * Returns a resolved object for this {@link OptionMapping}
	 *
	 * @param event The event of this interaction, could be a {@link SlashCommandInteractionEvent} or a {@link CommandAutoCompleteInteractionEvent}
	 * @param optionMapping The {@link OptionMapping} to be resolved
	 * @return The resolved option mapping
	 */
	@Nullable
	Object resolve(CommandInteractionPayload event, OptionMapping optionMapping);
}