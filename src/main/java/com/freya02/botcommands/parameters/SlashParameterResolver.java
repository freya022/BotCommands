package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import javax.annotation.Nullable;

/**
 * Interface which indicates this class can resolve parameters for slash commands
 */
public interface SlashParameterResolver {
	@Nullable
	Object resolve(SlashCommandEvent event, OptionMapping optionData);
}