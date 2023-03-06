package com.freya02.botcommands.api.modals.annotations;

import com.freya02.botcommands.api.modals.Modals;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that this parameter is queried from the modal inputs.
 * <br>The specified input name must be the same as the input name given in, for example, {@link Modals#createTextInput(String, String, TextInputStyle)}.
 *
 * @see ModalData
 * @see ModalHandler
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface ModalInput {
	/**
	 * The name of the modal input
	 * <br>Must match the input name provided in, for example, {@link Modals#createTextInput(String, String, TextInputStyle)}
	 *
	 * @return The name of the modal input
	 */
	String name();
}
