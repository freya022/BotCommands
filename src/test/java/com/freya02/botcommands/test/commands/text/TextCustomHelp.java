package com.freya02.botcommands.test.commands.text;

import com.freya02.botcommands.annotations.api.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.annotations.api.prefixed.annotations.TextOption;
import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;

public class TextCustomHelp extends TextCommand {
	@JDATextCommand(
			name = "help",
			description = "halp plz"
	)
	public void onTextHelp(BaseCommandEvent event,
	                       @TextOption String query) {
		event.respond("halp for: " + query).queue();
	}
}
