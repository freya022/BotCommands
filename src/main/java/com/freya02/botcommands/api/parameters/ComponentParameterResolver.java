package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.components.ComponentDescriptor;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface which indicates this class can resolve parameters for buttons commands
 */
public interface ComponentParameterResolver {
	/**
	 * Returns a resolved object from this component interaction
	 *
	 * @param context    The {@link BContext} of this bot
	 * @param descriptor The component description of the component being executed
	 * @param event      The event of this component interaction
	 * @return The resolved option mapping
	 */
	@Nullable
	Object resolve(@NotNull BContext context, @NotNull ComponentDescriptor descriptor, @NotNull GenericComponentInteractionCreateEvent event, @NotNull String arg);
}
