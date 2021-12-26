package com.freya02.bot.wiki.context.commands;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.message.GuildMessageEvent;
import net.dv8tion.jda.api.entities.Message;

public class ContextQuote extends ApplicationCommand {
	@JDAMessageCommand(name = "Quote message")
	public void execute(GuildMessageEvent event) {
		final Message targetMessage = event.getTarget();

		event.reply("> " + targetMessage.getContentRaw()).queue();
	}
}
