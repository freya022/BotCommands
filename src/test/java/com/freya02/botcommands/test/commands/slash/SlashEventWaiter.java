package com.freya02.botcommands.test.commands.slash;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import com.freya02.botcommands.api.core.waiter.EventWaiter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SlashEventWaiter extends ApplicationCommand {
	@JDASlashCommand(
			name = "event_waiter"
	)
	public void onSlashEventWaiter(GuildSlashEvent event, EventWaiter eventWaiter) {
		event.reply("Waiting for a message").setEphemeral(true).queue();

		eventWaiter.of(MessageReceivedEvent.class)
				.setOnSuccess(e -> event.getHook().setEphemeral(true).sendMessage("Received !").queue())
				.submit();
	}
}