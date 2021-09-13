package com.freya02.botcommands.api.parameters;

import net.dv8tion.jda.api.events.interaction.commands.UserContextCommandEvent;
import org.jetbrains.annotations.Nullable;

public interface UserContextParameterResolver {
	@Nullable
	Object resolve(UserContextCommandEvent event);
}
