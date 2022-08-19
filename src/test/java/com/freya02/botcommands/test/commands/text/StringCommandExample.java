package com.freya02.botcommands.test.commands.text;

import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.commands.prefixed.CommandEvent;
import com.freya02.botcommands.api.commands.prefixed.TextCommand;
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.api.commands.prefixed.annotations.TextOption;

public class StringCommandExample extends TextCommand {
	@JDATextCommand(
			name = "stringex"
	)
	public void execute(CommandEvent event) { }

	@JDATextCommand(
			name = "stringex"
	)
	public void execute(BaseCommandEvent event, @TextOption String content) {
		System.out.println("content = " + content);
	}
}
