package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.commands.application.slash.annotations.SlashOption;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;

public class SlashThread extends ApplicationCommand {
	@JDASlashCommand(name = "thread")
	public void run(GuildSlashEvent event,
	                @SlashOption ThreadChannel channel) {
		event.deferReply(true).queue();

		event.getHook().sendMessageFormat("<@%s> : %d messages and %d members",
						channel.getOwnerId(),
						channel.getMessageCount(),
						channel.getMemberCount())
				.queue();
	}
}
