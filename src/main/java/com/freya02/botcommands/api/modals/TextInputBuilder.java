package com.freya02.botcommands.api.modals;

import com.freya02.botcommands.internal.modals.InputData;
import com.freya02.botcommands.internal.modals.ModalMaps;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class TextInputBuilder extends TextInput.Builder {
	private final ModalMaps modalMaps;

	@ApiStatus.Internal
	public TextInputBuilder(ModalMaps modalMaps, String label, TextInputStyle style) {
		super("0", label, style);

		this.modalMaps = modalMaps;
	}

	@NotNull
	@Override
	public TextInput build() {
		final TextInput input = super.build();

		modalMaps.insertInput(new InputData());

		return input;
	}
}
