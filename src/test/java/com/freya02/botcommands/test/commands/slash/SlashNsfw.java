package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.annotations.api.annotations.NSFW;
import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.CommandScope;
import com.freya02.botcommands.api.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

public class SlashNsfw extends ApplicationCommand {
	@NSFW(dm = true)
	@JDASlashCommand(scope = CommandScope.GLOBAL, name = "nsfw")
	public void nsfw(GlobalSlashEvent event) {
		event.reply("nsfw content").setEphemeral(true).queue();
	}
}
