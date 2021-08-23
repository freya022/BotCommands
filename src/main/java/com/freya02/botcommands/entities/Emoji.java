package com.freya02.botcommands.entities;

import net.dv8tion.jda.api.entities.IMentionable;

/**
 * Represents an Unicode Emoji
 */
public interface Emoji extends IMentionable {
	String getUnicode();
}
