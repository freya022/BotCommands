package com.freya02.botcommands.api.prefixed;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface TextGeneratedValueSupplier {
	@Nullable
	Object getDefaultValue(@NotNull MessageReceivedEvent event); //TODO use BC event
}
