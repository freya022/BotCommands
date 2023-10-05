package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

public class SlashInvite extends ApplicationCommand {
	@JDASlashCommand(scope = CommandScope.GLOBAL, name = "invite")
	public void run(GlobalSlashEvent event) {
		event.reply("Here's the invite link ! <" + event.getJDA().getInviteUrl() + ">")
				.setEphemeral(true)
				.queue();
	}
}
