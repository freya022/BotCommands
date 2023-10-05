package io.github.freya022.botcommands.test.commands.text;

import io.github.freya022.botcommands.api.commands.prefixed.CommandEvent;
import io.github.freya022.botcommands.api.commands.prefixed.TextCommand;
import io.github.freya022.botcommands.api.commands.prefixed.annotations.JDATextCommand;
import io.github.freya022.botcommands.api.commands.prefixed.annotations.NSFW;

public class TextNSFW extends TextCommand {
	@NSFW
	@JDATextCommand(name = "nsfw")
	public void nsfw(CommandEvent event) {
		event.reply("nsfw content").queue();
	}
}
