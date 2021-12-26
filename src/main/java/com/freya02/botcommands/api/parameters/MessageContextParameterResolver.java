package com.freya02.botcommands.api.parameters;

import net.dv8tion.jda.api.events.interaction.command.MessageContextEvent;
import org.jetbrains.annotations.Nullable;

public interface MessageContextParameterResolver {
	@Nullable
	Object resolve(MessageContextEvent event);
}
