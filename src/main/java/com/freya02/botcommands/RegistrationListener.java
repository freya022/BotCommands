package com.freya02.botcommands;

import com.freya02.botcommands.buttons.ButtonDescriptor;
import com.freya02.botcommands.prefixed.Command;

public abstract class RegistrationListener {
	public abstract void onCommandRegistered(Command command);

	public abstract void onSubcommandRegistered(Command command);

	public abstract void onSlashCommandRegistered(net.dv8tion.jda.api.interactions.commands.Command command);

	public abstract void onGuildSlashCommandRegistered(net.dv8tion.jda.api.interactions.commands.Command command);

	public abstract void onButtonRegistered(ButtonDescriptor descriptor);
}
