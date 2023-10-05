package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

public class SlashDoNotRun extends ApplicationCommand {
	@JDASlashCommand(name = "donotrun")
	public void doNotRun(GuildSlashEvent event) {
		throw new IllegalStateException();
	}
}
