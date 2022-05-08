package com.freya02.botcommands.test;

import com.freya02.botcommands.annotations.api.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.api.prefixed.CommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;

public class OutCommand extends TextCommand {
	@JDATextCommand(name = "outcmd")
	public void run(CommandEvent event) {

	}
}
