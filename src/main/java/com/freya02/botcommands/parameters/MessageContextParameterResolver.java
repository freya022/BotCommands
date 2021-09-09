package com.freya02.botcommands.parameters;

import net.dv8tion.jda.api.events.interaction.commands.MessageContextCommandEvent;
import org.jetbrains.annotations.Nullable;

public interface MessageContextParameterResolver {
	@Nullable
	Object resolve(MessageContextCommandEvent event);
}
