package com.freya02.bot.extensionsbot.commands;

import com.freya02.botcommands.BContext;
import com.freya02.botcommands.prefixed.Command;
import com.freya02.botcommands.prefixed.CommandEvent;
import com.freya02.botcommands.prefixed.annotation.JdaCommand;
import com.freya02.botcommands.waiter.EventWaiter;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.concurrent.TimeUnit;

@JdaCommand(
		name = "simon",
		description = "Do what Simon says"
)
public class SimonSays extends Command {
	public SimonSays(BContext context) {
		super(context);
	}

	@Override
	protected void execute(CommandEvent event) {
		event.reply("Simon says: say `hi` in less than 5 seconds").queue(m -> {
			EventWaiter.of(GuildMessageReceivedEvent.class) //Listen to guild messages
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
