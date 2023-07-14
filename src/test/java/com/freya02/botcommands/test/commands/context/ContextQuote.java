package com.freya02.botcommands.test.commands.context;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.context.annotations.ContextOption;
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.commands.application.context.message.GuildMessageEvent;
import net.dv8tion.jda.api.entities.Message;

public class ContextQuote extends ApplicationCommand {
	@JDAMessageCommand(name = "Quote message")
	public void quote(GuildMessageEvent event, @ContextOption Message target) {
		event.reply("Quote: " + target.getContentRaw()).queue();
	}
}
