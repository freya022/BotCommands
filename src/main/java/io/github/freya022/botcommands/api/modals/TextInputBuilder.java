package io.github.freya022.botcommands.api.modals;

import io.github.freya022.botcommands.internal.modals.InputData;
import io.github.freya022.botcommands.internal.modals.ModalMaps;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class TextInputBuilder extends TextInput.Builder {
	private final ModalMaps modalMaps;
	private final String inputName;

	@ApiStatus.Internal
	public TextInputBuilder(ModalMaps modalMaps, String inputName, String label, TextInputStyle style) {
		super("0", label, style);

		this.modalMaps = modalMaps;
		this.inputName = inputName;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>You can still set a custom ID on this TextInputBuilder, this is an <b>optional</b> step
	 *
	 * <br>This could be useful if this modal gets closed by the user by mistake, as Discord caches the inputs by its modal ID (and input IDs),
	 * keeping the same ID might help the user not having to type things again
	 *
	 * <p><b>Pay attention, if the ID is the same then it means that inputs associated to that ID will be overwritten</b>,
	 * so you should do something like appending the interacting user's ID at the end of the modal ID
	 */
	@NotNull
	@Override
	public TextInputBuilder setId(@NotNull String customId) {
		super.setId(customId);

		return this;
	}

	@NotNull
	@Override
	public TextInput build() {
		final String actualId = modalMaps.insertInput(new InputData(inputName), getId());

		setId(actualId);

		return super.build();
	}
}
