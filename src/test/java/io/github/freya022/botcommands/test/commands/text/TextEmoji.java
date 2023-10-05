package io.github.freya022.botcommands.test.commands.text;

import io.github.freya022.botcommands.api.commands.prefixed.BaseCommandEvent;
import io.github.freya022.botcommands.api.commands.prefixed.TextCommand;
import io.github.freya022.botcommands.api.commands.prefixed.annotations.JDATextCommand;
import io.github.freya022.botcommands.api.commands.prefixed.annotations.TextOption;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class TextEmoji extends TextCommand {
	@JDATextCommand(
			name = "emoji",
			description = "No description"
	)
	public void onTextEmoji(BaseCommandEvent event, @TextOption Emoji emoji) {
		event.reply("Emoji: " + emoji.getFormatted()).queue();
	}
}