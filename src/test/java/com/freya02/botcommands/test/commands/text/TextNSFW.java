package com.freya02.botcommands.test.commands.text;

import com.freya02.botcommands.annotations.api.annotations.NSFW;
import com.freya02.botcommands.api.prefixed.CommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.annotations.JDATextCommand;

public class TextNSFW extends TextCommand {
	@NSFW
	@JDATextCommand(name = "nsfw")
	public void nsfw(CommandEvent event) {
		event.reply("nsfw content").queue();
	}
}
