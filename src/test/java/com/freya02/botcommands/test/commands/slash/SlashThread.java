package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.application.slash.annotations.JDASlashCommand;
import net.dv8tion.jda.api.entities.ThreadChannel;

public class SlashThread extends ApplicationCommand {
	@JDASlashCommand(name = "thread")
	public void run(GuildSlashEvent event,
	                @AppOption ThreadChannel channel) {
		event.deferReply(true).queue();

		event.getHook().sendMessageFormat("<@%s> : %d messages and %d members",
						channel.getOwnerId(),
						channel.getMessageCount(),
						channel.getMemberCount())
				.queue();
	}
}
