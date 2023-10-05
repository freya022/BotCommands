package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

public class SlashNsfw extends ApplicationCommand {
	@JDASlashCommand(scope = CommandScope.GLOBAL, nsfw = true, name = "nsfw")
	public void nsfw(GlobalSlashEvent event) {
		event.reply("nsfw content").setEphemeral(true).queue();
	}
}
