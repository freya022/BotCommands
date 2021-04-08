package com.freya02.bot.commands;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.Command;
import com.freya02.botcommands.CommandEvent;
import com.freya02.botcommands.annotation.JdaCommand;

@JdaCommand(
		name = "ping",
		category = "Misc",
		description = "Pong !"
)
public class Ping extends Command {
	protected Ping(BContext context) {
		super(context);
	}

	@Override
	protected void execute(CommandEvent event) {
		long sent = System.nanoTime();
		event.reply("Pong !").queue(m -> {
			long received = System.nanoTime();

			long pingNs = (received - sent) / 2;
			double pingMs = pingNs / 1000000.0;

			m.editMessage(String.format("Pong ! in %.2f ms", pingMs)).queue();
		});
	}
}
