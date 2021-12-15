package com.freya02.botcommands.test.commands.text;

import com.freya02.botcommands.api.prefixed.CommandEvent;
import com.freya02.botcommands.api.prefixed.TextCommand;
import com.freya02.botcommands.api.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.api.waiter.EventWaiter;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.concurrent.TimeUnit;

public class WaiterTest extends TextCommand {
	@JDATextCommand(
			name = "waiter"
	)
	public void execute(CommandEvent event) {
		EventWaiter.of(GuildMessageReceivedEvent.class)
				.setOnComplete((f, e, t) -> System.out.println("Completed"))
				.setOnTimeout(() -> System.err.println("Timeout"))
				.setOnSuccess(e -> System.out.println("Success"))
				.setOnCancelled(() -> System.err.println("Cancelled"))
				.setTimeout(1, TimeUnit.SECONDS)
				.addPrecondition(e -> e.getAuthor().getIdLong() == event.getAuthor().getIdLong())
				.submit();
	}
}
