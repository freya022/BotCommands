package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

/**
 * Interface which indicates this class can resolve parameters for buttons commands
 */
public interface ButtonParameterResolver {
	Object resolve(ButtonClickEvent event, String arg);
}
