package com.freya02.botcommands.api.modals;

import com.freya02.botcommands.internal.modals.InternalModals;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

public interface Modals {
	@NotNull
	static ModalBuilder create(@NotNull String title) {
		return InternalModals.create(title);
	}

	@NotNull
	static TextInputBuilder createTextInput(@NotNull String label, @NotNull TextInputStyle style) {
		return InternalModals.createTextInput(label, style);
	}
}
