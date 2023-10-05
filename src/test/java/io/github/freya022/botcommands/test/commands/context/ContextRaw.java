package io.github.freya022.botcommands.test.commands.context;

import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.CommandScope;
import io.github.freya022.botcommands.api.commands.application.context.annotations.ContextOption;
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand;
import io.github.freya022.botcommands.api.commands.application.context.message.GlobalMessageEvent;
import net.dv8tion.jda.api.entities.Message;

public class ContextRaw extends ApplicationCommand {
	@JDAMessageCommand(scope = CommandScope.GLOBAL, name = "Get raw message")
	public void raw(GlobalMessageEvent event, @ContextOption Message target) {
		event.reply(target.getContentRaw()).setEphemeral(true).queue();
	}
}
