package com.freya02.botcommands.api.modals.annotations;

import com.freya02.botcommands.api.modals.Modals;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that this method handles modals with the specified modal name
 *
 * <p>The method must:
 * <ul>
 *     <li>Be non-static and public</li>
 *     <li>Have {@link ModalInteractionEvent} as its first parameter</li>
 *     <li>Optionally: Have all your consecutive {@link ModalData}, specified in {@link Modals#create(String, Object...)}</li>
 *     <li>And finally: Have all your {@link ModalInput} and custom parameters, in the order you want</li>
 * </ul>
 *
 * @see ModalData
 * @see ModalInput
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ModalHandler {
	/**
	 * The name of the handler, must be the same as the handler name supplied in {@link Modals#create(String, String, Object...)}
	 *
	 * @return The name of the modal handler
	 */
	String name();
}
