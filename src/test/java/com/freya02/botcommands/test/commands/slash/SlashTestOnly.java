package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.annotations.Test;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

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
