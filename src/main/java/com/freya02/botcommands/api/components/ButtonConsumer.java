package com.freya02.botcommands.api.components;

import com.freya02.botcommands.api.components.event.ButtonEvent;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ButtonConsumer extends ComponentConsumer<ButtonEvent> {
	void accept(@NotNull ButtonEvent btnEvt) throws Exception;
}
