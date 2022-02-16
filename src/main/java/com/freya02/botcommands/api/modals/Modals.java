package com.freya02.botcommands.api.modals;

import com.freya02.botcommands.internal.BContextImpl;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

public class Modals {
	private static BContextImpl context;

	private Modals() {}

	static void setContext(BContextImpl context) {
		Modals.context = context;
	}

	@NotNull
	public static ModalBuilder create(@NotNull String title) {
		return new ModalBuilder(context.getModalMaps().newModalId(), title);
	}

	@NotNull
	public static TextInputBuilder createTextInput(@NotNull String label, @NotNull TextInputStyle style) {
		return new TextInputBuilder(context.getModalMaps().newInputId(), label, style);
	}
}
