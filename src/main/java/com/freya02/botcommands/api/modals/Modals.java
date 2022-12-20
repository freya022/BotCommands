package com.freya02.botcommands.api.modals;

import com.freya02.botcommands.api.core.annotations.InjectedService;
import com.freya02.botcommands.api.modals.annotations.ModalInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

/**
 * Methods for modals and modal inputs
 */
@InjectedService
public interface Modals {
	/**
	 * Creates a new modal with the specified handler name, and the passed user data
	 *
	 * @param title The title of the modal
	 *
	 * @return The new ModalBuilder
	 */
	@NotNull
	ModalBuilder create(@NotNull String title);

	/**
	 * Creates a new text input component
	 *
	 * @param inputName The name of the input, must match a {@link ModalInput}
	 * @param label     The label to display on top of the text field
	 * @param style     The style of the text field
	 *
	 * @return The new TextInputBuilder
	 */
	@NotNull
	TextInputBuilder createTextInput(@NotNull String inputName, @NotNull String label, @NotNull TextInputStyle style);
}
