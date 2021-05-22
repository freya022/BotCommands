package com.freya02.botcommands.buttons;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public interface KeyProvider {
	/**
	 * Gets the key used to decrypt buttons sent in a Guild
	 *
	 * @param guild Guild where the button is
	 * @return Key to decrypt this Guild's buttons
	 */
	Key getKey(Guild guild);

	/**
	 * Gets the key used to decrypt buttons sent in a User's DM
	 *
	 * @param user The User to which the button was sent in DMS
	 * @return Key to decrypt this User's buttons
	 */
	Key getKey(User user);
}
