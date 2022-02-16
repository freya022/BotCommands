package com.freya02.botcommands.test;

import com.freya02.botcommands.api.prefixed.CommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.annotations.JDATextCommand;

public class OutCommand extends TextCommand {
	@JDATextCommand(name = "outcmd")
	public void run(CommandEvent event) {

	}
}
