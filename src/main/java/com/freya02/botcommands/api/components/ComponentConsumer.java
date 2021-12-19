package com.freya02.botcommands.api.components;

import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import org.jetbrains.annotations.NotNull;

public interface ComponentConsumer<T extends GenericComponentInteractionCreateEvent> {
	void accept(@NotNull T t) throws Exception;
}
