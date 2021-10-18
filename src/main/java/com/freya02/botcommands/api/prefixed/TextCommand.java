package com.freya02.botcommands.api.prefixed;

import com.freya02.botcommands.api.CommandsBuilder;
import com.freya02.botcommands.api.prefixed.annotations.JDATextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * <p>Extend this class on classes that contains any command / subcommand</p>
 * <p>You also need to use the {@linkplain JDATextCommand @JDATextCommand} annotation on methods in order to register a command with {@linkplain CommandsBuilder}</p>
 */
public abstract class TextCommand {
	/**
	 * <p>Returns a detailed embed of what the command is, it is used by the internal <code>'help'</code> command</p>
	 * <p>The <code>'help'</code> command will automatically set the embed title to be "<code>Command '[command_name]'</code>" but can be overridden</p>
	 * <p>It will also set the embed's description to be the command's description, <b>you can override with {@link EmbedBuilder#setDescription(CharSequence)}</b></p>
	 *
	 * @return The EmbedBuilder to use as a detailed description
	 */
	@Nullable
	public Consumer<EmbedBuilder> getDetailedDescription() {return null;}
}
