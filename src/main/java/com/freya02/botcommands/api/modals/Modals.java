package com.freya02.botcommands.api.modals;

import com.freya02.botcommands.annotations.api.modals.annotations.ModalHandler;
import com.freya02.botcommands.annotations.api.modals.annotations.ModalInput;
import com.freya02.botcommands.internal.modals.InternalModals;
import com.freya02.botcommands.internal.modals.ModalData;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

/**
 * Factory methods for modals and modal inputs
 */
public interface Modals {
	/**
	 * Creates a new modal with the specified handler name, and the passed user data
	 *
	 * @param title       The title of the modal
	 * @param handlerName The name of the modal handler, must be the same as your {@link ModalHandler}
	 * @param userData    The optional user data to be passed to the modal handler via {@link ModalData}
	 *
	 * @return The new ModalBuilder
	 */
	@NotNull
	static ModalBuilder create(@NotNull String title, @NotNull String handlerName, Object... userData) {
		return InternalModals.create(title, handlerName, userData);
	}

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
	static TextInputBuilder createTextInput(@NotNull String inputName, @NotNull String label, @NotNull TextInputStyle style) {
		return InternalModals.createTextInput(inputName, label, style);
	}
}
