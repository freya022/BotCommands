package io.github.freya022.botcommands.test;

import io.github.freya022.botcommands.api.commands.prefixed.CommandEvent;
import io.github.freya022.botcommands.api.commands.prefixed.TextCommand;
import io.github.freya022.botcommands.api.commands.prefixed.annotations.JDATextCommand;

public class OutCommand extends TextCommand {
	@JDATextCommand(name = "outcmd")
	public void run(CommandEvent event) {

	}
}
