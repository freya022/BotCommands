package io.github.freya022.botcommands.test.commands.slash;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.core.waiter.EventWaiter;
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