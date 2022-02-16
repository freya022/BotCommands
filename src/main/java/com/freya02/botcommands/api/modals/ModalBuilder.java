package com.freya02.botcommands.api.modals;

import net.dv8tion.jda.api.interactions.components.text.Modal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ModalBuilder extends Modal.Builder {
	ModalBuilder(String modalId, String title) {
		super(modalId);

		setTitle(title);
	}

	@NotNull
	@Override
	@Contract("_ -> fail")
	public Modal.Builder setId(@NotNull String customId) {
		throw new IllegalStateException("Modal ID is already set on this builder");
	}
}
