package com.freya02.bot.pingbot.commands;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.api.prefixed.Command;
import com.freya02.botcommands.api.prefixed.CommandEvent;
import com.freya02.botcommands.api.prefixed.annotations.JdaCommand;

@JdaCommand(
		name = "ping",
		category = "Misc",
		description = "Pong !"
)
public class Ping extends Command {
	public Ping(BContext context) {
		super(context);
	}

	@Override
	protected void execute(CommandEvent event) {
		final long gatewayPing = event.getJDA().getGatewayPing();

		event.getJDA().getRestPing()
				.queue(restPing -> event.respondFormat("Gateway ping: **%d ms**\nRest ping: **%d ms**", gatewayPing, restPing).queue());
	}
}
