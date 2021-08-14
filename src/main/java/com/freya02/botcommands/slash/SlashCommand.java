package com.freya02.botcommands.slash;

import com.freya02.botcommands.BContext;

/**
 * Every slash command has to inherit this class
 */
public abstract class SlashCommand implements GuildSlashSettings {
	public SlashCommand() {}

	public SlashCommand(BContext context) { }
}
