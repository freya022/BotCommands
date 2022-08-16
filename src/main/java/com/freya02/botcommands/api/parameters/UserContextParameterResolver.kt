package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UserContextParameterResolver {
	/**
	 * Returns a resolved object from this user context interaction
	 *
	 * @param context The {@link BContext} of this bot
	 * @param info    The user command info of the command being executed
	 * @param event   The event of this user context interaction
	 * @return The resolved option mapping
	 */
	@Nullable
	Object resolve(@NotNull BContext context, @NotNull UserCommandInfo info, @NotNull UserContextInteractionEvent event);
}
