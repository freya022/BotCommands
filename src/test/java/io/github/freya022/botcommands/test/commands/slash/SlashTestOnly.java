package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.annotations.Test;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

public class SlashTestOnly extends ApplicationCommand {
	@Test
	@JDASlashCommand(
			name = "test-only"
	)
	public void testOnly(GuildSlashEvent event) {
		if (event.getGuild().getIdLong() != 722891685755093072L) {
			throw new IllegalArgumentException("Not the test guild ID");
		}

		event.reply("In a test guild").setEphemeral(true).queue();
	}
}
