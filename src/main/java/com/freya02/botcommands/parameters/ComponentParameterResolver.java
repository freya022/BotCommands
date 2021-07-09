package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;

/**
 * Interface which indicates this class can resolve parameters for buttons commands
 */
public interface ComponentParameterResolver {
	Object resolve(GenericComponentInteractionCreateEvent event, String arg);
}
