package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.events.interaction.commands.MessageContextCommandEvent;

import javax.annotation.Nullable;

public interface MessageContextParameterResolver {
	@Nullable
	Object resolve(MessageContextCommandEvent event);
}
