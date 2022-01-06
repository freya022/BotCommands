package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.application.context.user.UserCommandInfo;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface UserContextParameterResolver {
	@Nullable
	Object resolve(@NotNull BContext context, @NotNull UserCommandInfo info, @NotNull UserContextInteractionEvent event);
}
