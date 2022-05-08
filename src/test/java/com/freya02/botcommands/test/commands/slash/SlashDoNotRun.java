package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.annotations.api.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;

public class SlashDoNotRun extends ApplicationCommand {
	@JDASlashCommand(name = "donotrun")
	public void doNotRun(GuildSlashEvent event) {
		throw new IllegalStateException();
	}
}
