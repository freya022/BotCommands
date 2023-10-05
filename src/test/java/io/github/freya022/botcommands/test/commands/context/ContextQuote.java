package io.github.freya022.botcommands.test.commands.context;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.context.annotations.ContextOption;
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand;
import io.github.freya022.botcommands.api.commands.application.context.message.GuildMessageEvent;
import net.dv8tion.jda.api.entities.Message;

public class ContextQuote extends ApplicationCommand {
	@JDAMessageCommand(name = "Quote message")
	public void quote(GuildMessageEvent event, @ContextOption Message target) {
		event.reply("Quote: " + target.getContentRaw()).queue();
	}
}
