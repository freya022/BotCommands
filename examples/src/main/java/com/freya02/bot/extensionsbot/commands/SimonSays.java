package com.freya02.bot.extensionsbot.commands;

import com.freya02.botcommands.api.commands.prefixed.CommandEvent;
import com.freya02.botcommands.api.commands.prefixed.TextCommand;
import com.freya02.botcommands.api.commands.prefixed.annotations.Description;
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand;
import com.freya02.botcommands.api.waiter.EventWaiter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.TimeUnit;

@Description("Do what Simon says")
public class SimonSays extends TextCommand {
	@JDATextCommand(name = "simon")
	public void execute(CommandEvent event) {
		event.reply("Simon says: say `hi` in less than 5 seconds").queue(m -> {
			EventWaiter.of(MessageReceivedEvent.class) //Listen to guild messages
					//Check for the same channel, same author, with message "hi"
					.addPrecondition(e -> e.getChannel().equals(event.getChannel()) && e.getAuthor().equals(event.getAuthor()) && e.getMessage().getContentRaw().equals("hi"))
					//Expire on 5 seconds
					.setTimeout(5, TimeUnit.SECONDS)
					//After event waiter has expired, send a timeout message
					.setOnTimeout(() -> m.reply("Timeout !").queue())
					//After the preconditions have been fulfilled, reply
					.setOnSuccess(e -> e.getMessage().reply("n i c e").queue())
					//Wait for the event (non-blocking)
					.submit();
		});
	}
}
