package io.github.freya022.botcommands.test.commands.text;

import io.github.freya022.botcommands.api.commands.prefixed.CommandEvent;
import io.github.freya022.botcommands.api.commands.prefixed.TextCommand;
import io.github.freya022.botcommands.api.commands.prefixed.annotations.JDATextCommand;
import io.github.freya022.botcommands.api.core.BContext;
import net.dv8tion.jda.api.entities.Message;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class CtorInjectionTest extends TextCommand {
	public CtorInjectionTest(BContext context, LocalDateTime start) {
		System.out.println("context = " + context);
		System.out.println("start = " + start);
	}

	@JDATextCommand(
			name = "ctorinjection"
	)
	public void run(CommandEvent event) {
		event.reply("ok")
				.delay(5, TimeUnit.SECONDS)
				.flatMap(Message::delete)
				.queue();
	}
}