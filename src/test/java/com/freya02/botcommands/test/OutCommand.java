package com.freya02.botcommands.test;

import com.freya02.botcommands.api.commands.prefixed.CommandEvent;
import com.freya02.botcommands.api.commands.prefixed.TextCommand;
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand;

public class OutCommand extends TextCommand {
	@JDATextCommand(name = "outcmd")
	public void run(CommandEvent event) {

	}
}
