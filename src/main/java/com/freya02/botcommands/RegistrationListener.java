package com.freya02.botcommands;

import com.freya02.botcommands.buttons.ButtonDescriptor;
import com.freya02.botcommands.prefixed.Command;
import com.freya02.botcommands.slash.SlashCommandInfo;

public abstract class RegistrationListener {
	public abstract void onCommandRegistered(Command command);

	public abstract void onSubcommandRegistered(Command command);

	public abstract void onSlashCommandRegistered(SlashCommandInfo slashCommandInfo);

	public abstract void onButtonRegistered(ButtonDescriptor descriptor);
}
