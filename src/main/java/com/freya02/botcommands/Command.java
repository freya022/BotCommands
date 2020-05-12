package com.freya02.botcommands;

import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * <p>Extend this class on the class used for a <b>command or a subcommand</b></p>
 * <p>You also need to use the {@linkplain com.freya02.botcommands.annotation.JdaCommand @JdaCommand} annotation in order to register a command with {@linkplain CommandsBuilder}</p>
 */
public abstract class Command {
	/** Called when the command is invoked by an user
	 *
	 * @param event {@linkplain CommandEvent} object for gathering arguments / author / channel / etc...
	 */
	protected abstract void execute(CommandEvent event);

	/**
	 * <p>Returns a detailed embed of what the command is, it is used by the internal 'help' command</p>
	 * <p>The 'help' command will automatically set the embed title to be "<code>Command '[command_name]'</code>" but can be overridden</p>
	 * <p>It will also set the embed's description to be the command's description, <b>you can override with {@linkplain EmbedBuilder#setDescription(CharSequence)}</b></p>
	 * @return The EmbedBuilder to use as a detailed description
	 */
	@Nullable
	protected Consumer<EmbedBuilder> getDetailedDescription() { return null; }
}
