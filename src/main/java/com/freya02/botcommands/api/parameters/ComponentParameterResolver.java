package com.freya02.botcommands.api.parameters;

import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Interface which indicates this class can resolve parameters for buttons commands
 */
public interface ComponentParameterResolver {
	@Nullable
	Object resolve(GenericComponentInteractionCreateEvent event, String arg);
}
