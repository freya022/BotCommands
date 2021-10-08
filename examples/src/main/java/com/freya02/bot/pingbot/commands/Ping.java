package com.freya02.bot.pingbot.commands;

import com.freya02.botcommands.api.prefixed.CommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.annotations.Category;
import com.freya02.botcommands.api.prefixed.annotations.Description;
import com.freya02.botcommands.api.prefixed.annotations.JdaTextCommand;

@Category("Misc")
@Description("Pong !")
public class Ping extends TextCommand {
	@JdaTextCommand(name = "ping")
	public void execute(CommandEvent event) {
		final long gatewayPing = event.getJDA().getGatewayPing();

		event.getJDA().getRestPing()
				.queue(restPing -> event.respondFormat("Gateway ping: **%d ms**\nRest ping: **%d ms**", gatewayPing, restPing).queue());
	}
}

