package io.github.freya022.botcommands.test.commands.text;

import io.github.freya022.botcommands.api.commands.prefixed.BaseCommandEvent;
import io.github.freya022.botcommands.api.commands.prefixed.CommandEvent;
import io.github.freya022.botcommands.api.commands.prefixed.TextCommand;
import io.github.freya022.botcommands.api.commands.prefixed.annotations.JDATextCommand;
import io.github.freya022.botcommands.api.commands.prefixed.annotations.TextOption;

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
