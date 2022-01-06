package com.freya02.botcommands.api.parameters;

import com.freya02.botcommands.api.BContext;
import com.freya02.botcommands.internal.application.context.message.MessageCommandInfo;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MessageContextParameterResolver {
	@Nullable
	Object resolve(@NotNull BContext context, @NotNull MessageCommandInfo info, @NotNull MessageContextInteractionEvent event);
}
