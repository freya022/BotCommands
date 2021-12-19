package com.freya02.botcommands.api.components;

import com.freya02.botcommands.api.components.event.SelectionEvent;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface SelectionConsumer extends ComponentConsumer<SelectionEvent> {
	void accept(@NotNull SelectionEvent selectEvt) throws Exception;
}
