package com.freya02.botcommands.api.components;

import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface SelectionConsumer<T extends GenericSelectMenuInteractionEvent<?, ?>> extends ComponentConsumer<T> {
	void accept(@NotNull T selectEvt) throws Exception;
}
