package com.freya02.botcommands;

import com.freya02.botcommands.buttons.ButtonDescriptor;
import com.freya02.botcommands.prefixed.Command;
import net.dv8tion.jda.api.entities.Guild;

public abstract class RegistrationListener {
	/**
	 * Fired when a regular command is registered
	 *
	 * @param command Command which got registered
	 */
	public abstract void onCommandRegistered(Command command);

	/**
	 * Fired when a regular subcommand is registered
	 *
	 * @param command Command which got registered
	 */
	public abstract void onSubcommandRegistered(Command command);

	/**
	 * Fired when Discord acknowledged the slash command globally
	 *
	 * @param command Slash command which got registered
	 */
	public abstract void onGlobalSlashCommandRegistered(net.dv8tion.jda.api.interactions.commands.Command command);

	/**
	 * Fired when Discord acknowledged the slash command in this {@linkplain Guild}
	 *
	 * @param guild   Guild in which the command was registered in
	 * @param command Slash command which got registered
	 */
	public abstract void onGuildSlashCommandRegistered(Guild guild, net.dv8tion.jda.api.interactions.commands.Command command);

	/**
	 * Fired when a button listener is registered
	 *
	 * @param descriptor {@linkplain ButtonDescriptor} of the registered button
	 */
	public abstract void onButtonRegistered(ButtonDescriptor descriptor);

	/**
	 * Fired when {@linkplain CommandsBuilder} has finished building
	 */
	public abstract void onBuildComplete();
}
