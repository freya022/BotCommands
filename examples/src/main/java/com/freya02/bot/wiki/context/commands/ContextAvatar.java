package com.freya02.bot.wiki.context.commands;

import com.freya02.botcommands.api.application.ApplicationCommand;
import com.freya02.botcommands.api.application.context.annotations.JDAUserCommand;
import com.freya02.botcommands.api.application.context.user.GuildUserEvent;
import net.dv8tion.jda.api.entities.User;

public class ContextAvatar extends ApplicationCommand {
	@JDAUserCommand(name = "Get avatar")
	public void execute(GuildUserEvent event) {
		final User targetUser = event.getTarget();

		event.reply(targetUser.getEffectiveAvatarUrl()).queue();
	}
}
