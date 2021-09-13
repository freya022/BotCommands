package com.freya02.botcommands.api;

import com.freya02.botcommands.api.prefixed.Command;
import com.freya02.botcommands.internal.components.ComponentDescriptor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.SlashCommand;

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
	 * Fired when Discord acknowledged the application command globally
	 *
	 * @param command Slash command which got registered
	 */
	public abstract void onGlobalSlashCommandRegistered(SlashCommand command);

	/**
	 * Fired when Discord acknowledged the application command in this {@linkplain Guild}
	 *
	 * @param guild   Guild in which the command was registered in
	 * @param command Slash command which got registered
	 */
	public abstract void onGuildSlashCommandRegistered(Guild guild, SlashCommand command);

	/**
	 * Fired when the bot doesn't have the applications.commands scope in a Guild, thus is unable to register the application commands
	 *
	 * @param guild Guild in which the application commands couldn't be registered
	 * @param inviteUrl The invite URL to invite back the bot in the same Guild, with the same permissions, with the correct scopes (bot + applications.commands)
	 */
	public abstract void onGuildSlashCommandMissingAccess(Guild guild, String inviteUrl);

	/**
	 * Fired when a component listener is registered
	 *
	 * @param descriptor {@linkplain ComponentDescriptor} of the registered component
	 */
	public abstract void onComponentRegistered(ComponentDescriptor descriptor);

	/**
	 * Fired when {@linkplain CommandsBuilder} has finished building
	 */
	public abstract void onBuildComplete();
}
