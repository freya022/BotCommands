package com.freya02.bot.pingbot.commands;

import com.freya02.botcommands.api.commands.prefixed.CommandEvent;
import com.freya02.botcommands.api.commands.prefixed.TextCommand;
import com.freya02.botcommands.api.commands.prefixed.annotations.Category;
import com.freya02.botcommands.api.commands.prefixed.annotations.Description;
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand;

@Category("Misc")
@Description("Pong !")
public class Ping extends TextCommand {
	@JDATextCommand(name = "ping")
	public void execute(CommandEvent event) {
		final long gatewayPing = event.getJDA().getGatewayPing();

		event.getJDA().getRestPing()
				.queue(restPing -> event.respondFormat("Gateway ping: **%d ms**\nRest ping: **%d ms**", gatewayPing, restPing).queue());
	}
}

