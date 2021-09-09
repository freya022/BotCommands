package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.events.interaction.commands.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.Nullable;

/**
 * Interface which indicates this class can resolve parameters for application commands
 */
public interface SlashParameterResolver {
	@Nullable
	Object resolve(SlashCommandEvent event, OptionMapping optionMapping);
}