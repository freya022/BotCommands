package com.freya02.botcommands.buttons;

import com.freya02.botcommands.BContext;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

/**
 * Consumer used when a button is clicked
 */
@FunctionalInterface
public interface ButtonConsumer {
	/**
	 * Fired when a button is clicked and conditions are met (if clicked by a certain user for example)
	 *
	 * @param context The {@linkplain BContext} instance
	 * @param event   The {@linkplain ButtonClickEvent} provided by JDA
	 */
	void accept(BContext context, ButtonClickEvent event);
}
