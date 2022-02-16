package com.freya02.botcommands.api.modals;

import com.freya02.botcommands.internal.modals.InternalModals;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

public interface Modals {
	@NotNull
	static ModalBuilder create(@NotNull String handlerName, Object... userData) {
		return InternalModals.create(handlerName, userData);
	}

	@NotNull
	static TextInputBuilder createTextInput(@NotNull String inputName, @NotNull String label, @NotNull TextInputStyle style) {
		return InternalModals.createTextInput(inputName, label, style);
	}
}
