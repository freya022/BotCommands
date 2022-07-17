package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;

public class SlashInvite extends ApplicationCommand {
	@JDASlashCommand(scope = CommandScope.GLOBAL, name = "invite")
	public void run(GlobalSlashEvent event) {
		event.reply("Here's the invite link ! <" + event.getJDA().getInviteUrl() + ">")
				.setEphemeral(true)
				.queue();
	}
}
