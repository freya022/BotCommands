package com.freya02.botcommands.test.commands.context;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.annotations.AppOption;
import com.freya02.botcommands.api.application.context.annotations.JDAMessageCommand;
import com.freya02.botcommands.api.application.context.message.GlobalMessageEvent;
import net.dv8tion.jda.api.entities.Message;

public class ContextRaw extends ApplicationCommand {
	@JDAMessageCommand(guildOnly = false, name = "Get raw message")
	public void raw(GlobalMessageEvent event, @AppOption Message target) {
		event.reply(target.getContentRaw()).setEphemeral(true).queue();
	}
}
