package com.freya02.botcommands.api.parameters;

import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.jetbrains.annotations.Nullable;

public interface UserContextParameterResolver {
	@Nullable
	Object resolve(UserContextInteractionEvent event);
}
