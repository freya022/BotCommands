package com.freya02.botcommands;

import net.dv8tion.jda.api.EmbedBuilder;

import java.util.function.Consumer;

public abstract class Command {
	public abstract void execute(CommandEvent event);

	/**
	 * <p>Returns a detailed embed of what the command is, it is used by the internal 'help' command</p>
	 * <p>This method will automatically set the embed title to be "<code>Command '[command_name]'</code>"</p>
	 * <p>It will also set the embed's description to be the command's description, <b>you can override with {@linkplain EmbedBuilder#setDescription(CharSequence)}</b></p>
	 * @return The EmbedBuilder to use as a detailed description
	 */
	public Consumer<EmbedBuilder> getDetailedDescription() { return null; }
}
