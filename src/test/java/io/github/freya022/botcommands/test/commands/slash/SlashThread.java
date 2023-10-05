package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
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
