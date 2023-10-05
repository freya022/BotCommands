package io.github.freya022.botcommands.test.commands.text;

import io.github.freya022.botcommands.api.commands.prefixed.CommandEvent;
import io.github.freya022.botcommands.api.commands.prefixed.TextCommand;
import io.github.freya022.botcommands.api.commands.prefixed.annotations.Category;
import io.github.freya022.botcommands.api.commands.prefixed.annotations.JDATextCommand;

@Category("Misc")
public class Ping extends TextCommand {
	@JDATextCommand(
			name = "ping",
			description = "Pong !"
	)
	public void execute(CommandEvent event) {
		long sent = System.nanoTime();
		event.reply("Pong !").queue(m -> {
			long received = System.nanoTime();

			long pingNs = (received - sent) / 2;
			double pingMs = pingNs / 1000000.0;

			m.editMessage(String.format("Pong ! in %.2f ms", pingMs)).queue();
		});
	}
}