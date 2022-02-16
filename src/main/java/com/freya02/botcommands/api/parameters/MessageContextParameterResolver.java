package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MessageContextParameterResolver {
	/**
	 * Returns a resolved object from this message context interaction
	 *
	 * @param context The {@link BContext} of this bot
	 * @param info    The message command info of the command being executed
	 * @param event   The event of this message context interaction
	 * @return The resolved option mapping
	 */
	@Nullable
	Object resolve(@NotNull BContext context, @NotNull MessageCommandInfo info, @NotNull MessageContextInteractionEvent event);
}
