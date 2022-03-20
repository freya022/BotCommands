package com.freya02.botcommands.test.commands.text;

import com.freya02.botcommands.api.prefixed.BaseCommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.api.prefixed.annotations.TextOption;

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
