package com.freya02.botcommands.test.commands.context;

import com.freya02.botcommands.api.commands.application.ApplicationCommand;
import com.freya02.botcommands.api.commands.application.CommandScope;
import com.freya02.botcommands.api.commands.application.annotations.AppOption;
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.commands.application.context.message.GlobalMessageEvent;
import net.dv8tion.jda.api.entities.Message;

public class ContextRaw extends ApplicationCommand {
	@JDAMessageCommand(scope = CommandScope.GLOBAL, name = "Get raw message")
	public void raw(GlobalMessageEvent event, @AppOption Message target) {
		event.reply(target.getContentRaw()).setEphemeral(true).queue();
	}
}
