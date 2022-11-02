package com.freya02.bot.wiki.slash.commands;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.CommandScope;
import com.freya02.botcommands.api.commands.application.slash.GlobalSlashEvent;
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand;

public class SlashPing extends ApplicationCommand {
	@JDASlashCommand(
			scope = CommandScope.GLOBAL,
			name = "ping",
			description = "Pong !"
	)
	public void onPing(GlobalSlashEvent event) {
		event.deferReply().queue();

		final long gatewayPing = event.getJDA().getGatewayPing();
		event.getJDA().getRestPing()
				.queue(l -> event.getHook()
						.sendMessageFormat("Gateway ping: **%d ms**\nRest ping: **%d ms**", gatewayPing, l)
						.queue());
	}
}