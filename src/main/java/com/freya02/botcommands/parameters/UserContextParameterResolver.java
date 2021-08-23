package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.events.interaction.commands.UserContextCommandEvent;

import javax.annotation.Nullable;

public interface UserContextParameterResolver {
	@Nullable
	Object resolve(UserContextCommandEvent event);
}
